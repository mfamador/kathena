package com.github.mfamador.kathena.controller

import com.github.mfamador.kathena.model.Article
import com.github.mfamador.kathena.model.Person
import com.github.mfamador.kathena.messaging.EventProducer
import com.github.mfamador.kathena.service.ArticleService
import com.github.mfamador.kathena.service.PersonService
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono

data class LoadDataResponse(
    val personsLoaded: Int,
    val articlesLoaded: Int,
    val messagesLoaded: Int,
    val message: String = "Data loaded successfully"
)

@RestController
@RequestMapping("/api/data")
class DataLoaderController(
    private val personService: PersonService,
    private val articleService: ArticleService,
    private val eventProducer: EventProducer
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @PostMapping("/load")
    fun loadSampleData(): Mono<LoadDataResponse> {
        log.info("Loading sample data into all stores...")

        // Sample persons for MongoDB
        val persons = listOf(
            Person(name = "Alice Johnson", email = "alice@example.com", age = 28, city = "New York"),
            Person(name = "Bob Smith", email = "bob@example.com", age = 35, city = "San Francisco"),
            Person(name = "Charlie Brown", email = "charlie@example.com", age = 42, city = "Chicago"),
            Person(name = "Diana Prince", email = "diana@example.com", age = 31, city = "Seattle"),
            Person(name = "Eve Adams", email = "eve@example.com", age = 26, city = "Boston"),
            Person(name = "Frank Miller", email = "frank@example.com", age = 45, city = "Austin"),
            Person(name = "Grace Hopper", email = "grace@example.com", age = 38, city = "New York"),
            Person(name = "Henry Ford", email = "henry@example.com", age = 52, city = "Detroit"),
            Person(name = "Iris West", email = "iris@example.com", age = 29, city = "Portland"),
            Person(name = "Jack Ryan", email = "jack@example.com", age = 33, city = "Miami")
        )

        // Sample articles for Elasticsearch
        val articles = listOf(
            Article(
                title = "Getting Started with Kotlin",
                content = "Kotlin is a modern programming language that makes developers happier.",
                author = "Alice Johnson",
                tags = listOf("kotlin", "programming", "tutorial")
            ),
            Article(
                title = "Spring WebFlux Deep Dive",
                content = "Learn about reactive programming with Spring WebFlux and Project Reactor.",
                author = "Bob Smith",
                tags = listOf("spring", "webflux", "reactive")
            ),
            Article(
                title = "MongoDB for Modern Applications",
                content = "Discover how MongoDB's document model enables flexible schema design.",
                author = "Charlie Brown",
                tags = listOf("mongodb", "database", "nosql")
            ),
            Article(
                title = "Elasticsearch Full-Text Search",
                content = "Master full-text search capabilities with Elasticsearch.",
                author = "Diana Prince",
                tags = listOf("elasticsearch", "search", "lucene")
            ),
            Article(
                title = "Kafka Streaming Architecture",
                content = "Build scalable event-driven systems with Apache Kafka.",
                author = "Eve Adams",
                tags = listOf("kafka", "streaming", "events")
            ),
            Article(
                title = "Microservices Best Practices",
                content = "Learn patterns and practices for building robust microservices.",
                author = "Frank Miller",
                tags = listOf("microservices", "architecture", "patterns")
            ),
            Article(
                title = "Reactive Programming Patterns",
                content = "Explore reactive patterns with Project Reactor and RxJava.",
                author = "Grace Hopper",
                tags = listOf("reactive", "patterns", "reactor")
            ),
            Article(
                title = "Docker for Developers",
                content = "Containerize your applications with Docker best practices.",
                author = "Henry Ford",
                tags = listOf("docker", "containers", "devops")
            ),
            Article(
                title = "Kotlin Coroutines Guide",
                content = "Master asynchronous programming with Kotlin coroutines.",
                author = "Iris West",
                tags = listOf("kotlin", "coroutines", "async")
            ),
            Article(
                title = "API Design Principles",
                content = "Design clean and maintainable REST APIs following best practices.",
                author = "Jack Ryan",
                tags = listOf("api", "rest", "design")
            )
        )

        // Sample Kafka messages
        val messages = listOf(
            "User registration event: Alice Johnson",
            "Article published: Getting Started with Kotlin",
            "User login event: Bob Smith",
            "Article published: Spring WebFlux Deep Dive",
            "User profile updated: Charlie Brown",
            "Search query: kotlin reactive programming",
            "Article viewed: MongoDB for Modern Applications",
            "User logout event: Diana Prince",
            "Article liked: Elasticsearch Full-Text Search",
            "System health check: All systems operational"
        )

        // Load persons into MongoDB
        val personsMono = personService.saveAll(persons)
            .collectList()
            .doOnSuccess { log.info("Loaded {} persons into MongoDB", it.size) }

        // Load articles into Elasticsearch
        val articlesMono = articleService.saveAll(articles)
            .collectList()
            .doOnSuccess { log.info("Loaded {} articles into Elasticsearch", it.size) }

        // Send messages to Kafka
        val messagesMono = Mono.fromRunnable<Void> {
            messages.forEach { message ->
                eventProducer.send(message)
                log.info("Sent message to Kafka: {}", message)
            }
        }.then(Mono.just(messages.size))

        return Mono.zip(personsMono, articlesMono, messagesMono)
            .map { tuple ->
                LoadDataResponse(
                    personsLoaded = tuple.t1.size,
                    articlesLoaded = tuple.t2.size,
                    messagesLoaded = tuple.t3
                )
            }
            .doOnSuccess { log.info("Sample data loading completed") }
    }

    @DeleteMapping("/clear")
    fun clearAllData(): Mono<String> {
        log.info("Clearing all data from stores...")

        return personService.deleteAll()
            .then(articleService.deleteAll())
            .then(Mono.just("All data cleared successfully"))
            .doOnSuccess { log.info("Data clearing completed") }
    }

    @GetMapping("/stats")
    fun getDataStats(): Mono<Map<String, Long>> {
        return Mono.zip(
            personService.count(),
            articleService.count()
        ).map { tuple ->
            mapOf(
                "persons" to tuple.t1,
                "articles" to tuple.t2
            )
        }
    }
}

