package com.github.mfamador.kathena.migration

import com.github.mfamador.kathena.model.Article
import com.github.mfamador.kathena.model.Person
import org.slf4j.LoggerFactory
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.data.annotation.Id
import org.springframework.data.domain.Sort
import org.springframework.data.elasticsearch.core.ElasticsearchOperations
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.core.index.Index
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers
import java.time.Instant

private const val MIGRATION_COLLECTION = "schema_migrations"

data class MigrationRecord(
    @Id val id: String,
    val name: String,
    val appliedAt: Instant = Instant.now()
)

data class Migration(
    val id: String,
    val name: String,
    val apply: () -> Mono<Void>
)

@Component
class MigrationRunner(
    private val mongoTemplate: ReactiveMongoTemplate,
    private val elasticsearchOperations: ElasticsearchOperations
) : ApplicationRunner {
    private val log = LoggerFactory.getLogger(javaClass)

    override fun run(args: ApplicationArguments) {
        val migrations = listOf(
            Migration(
                id = "mongo.person.dedup_email",
                name = "Remove duplicate Person emails"
            ) {
                mongoTemplate.aggregate(
                    org.springframework.data.mongodb.core.aggregation.Aggregation.newAggregation(
                        org.springframework.data.mongodb.core.aggregation.Aggregation.match(
                            Criteria.where("email").ne(null)
                        ),
                        org.springframework.data.mongodb.core.aggregation.Aggregation.group("email")
                            .count().`as`("count")
                            .first("email").`as`("email"),
                        org.springframework.data.mongodb.core.aggregation.Aggregation.match(
                            Criteria.where("count").gt(1)
                        )
                    ),
                    Person::class.java,
                    DuplicateEmail::class.java
                ).flatMap { duplicate ->
                    val email = duplicate.email ?: return@flatMap Mono.empty<Void>()
                    val query = Query(Criteria.where("email").`is`(email))
                        .with(Sort.by(Sort.Direction.DESC, "createdAt"))
                    mongoTemplate.find(query, Person::class.java)
                        .skip(1)
                        .mapNotNull { it.id }
                        .collectList()
                        .flatMap { ids ->
                            if (ids.isEmpty()) Mono.empty() else mongoTemplate.remove(
                                Query(Criteria.where("id").`in`(ids)),
                                Person::class.java
                            ).then()
                        }
                }.then()
            },
            Migration(
                id = "mongo.person.email_unique",
                name = "Ensure unique index on Person.email"
            ) {
                mongoTemplate.indexOps(Person::class.java)
                    .ensureIndex(
                        Index()
                            .on("email", Sort.Direction.ASC)
                            .unique()
                            .sparse()
                    )
                    .then()
            },
            Migration(
                id = "mongo.person.city_age",
                name = "Ensure compound index on Person.city + Person.age"
            ) {
                mongoTemplate.indexOps(Person::class.java)
                    .ensureIndex(
                        Index()
                            .on("city", Sort.Direction.ASC)
                            .on("age", Sort.Direction.ASC)
                    )
                    .then()
            },
            Migration(
                id = "elastic.blog.create",
                name = "Ensure Elasticsearch index for Article exists"
            ) {
                Mono.fromCallable {
                    val indexOps = elasticsearchOperations.indexOps(Article::class.java)
                    if (!indexOps.exists()) {
                        indexOps.createWithMapping()
                    }
                }.subscribeOn(Schedulers.boundedElastic()).then()
            },
            Migration(
                id = "elastic.blog.mapping",
                name = "Ensure Elasticsearch mapping for Article"
            ) {
                Mono.fromCallable {
                    val indexOps = elasticsearchOperations.indexOps(Article::class.java)
                    indexOps.putMapping()
                }.subscribeOn(Schedulers.boundedElastic()).then()
            }
        )

        val appliedIds = mongoTemplate.findAll(MigrationRecord::class.java, MIGRATION_COLLECTION)
            .map { it.id }
            .collectList()
            .defaultIfEmpty(emptyList())

        appliedIds.flatMapMany { existing ->
            Flux.fromIterable(migrations.filterNot { existing.contains(it.id) })
                .concatMap { migration ->
                    log.info("Applying migration {} ({})", migration.id, migration.name)
                    migration.apply()
                        .then(
                            mongoTemplate.save(
                                MigrationRecord(id = migration.id, name = migration.name),
                                MIGRATION_COLLECTION
                            )
                        )
                        .doOnSuccess { log.info("Migration {} applied", migration.id) }
                        .then()
                }
        }.doOnError { log.error("Migration failed", it) }
            .blockLast()
    }
}

private data class DuplicateEmail(
    val email: String? = null,
    val count: Int = 0
)
