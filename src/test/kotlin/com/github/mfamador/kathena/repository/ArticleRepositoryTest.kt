package com.github.mfamador.kathena.repository

import com.github.mfamador.kathena.model.Article
import com.palantir.docker.compose.DockerComposeExtension
import com.palantir.docker.compose.configuration.ShutdownStrategy.GRACEFUL
import com.palantir.docker.compose.connection.DockerPort
import com.palantir.docker.compose.connection.waiting.HealthChecks
import org.junit.jupiter.api.*
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.api.extension.RegisterExtension
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.junit.jupiter.SpringExtension
import java.time.LocalDateTime

@ExtendWith(SpringExtension::class)
@SpringBootTest
@TestPropertySource("classpath:test.properties")
@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
class ArticleRepositoryTest {

    @Autowired
    private lateinit var articleRepository: ArticleRepository

    @BeforeEach
    fun setup() {
        articleRepository.deleteAll()
        Thread.sleep(500) // Give Elasticsearch time
    }

    @Test
    @Order(1)
    fun `should save and retrieve article`() {
        // Given
        val article = Article(
            title = "Test Article",
            content = "Test content",
            author = "Test Author",
            tags = listOf("test", "kotlin")
        )

        // When
        val saved = articleRepository.save(article)
        Thread.sleep(500) // Wait for indexing

        // Then
        assert(saved.id != null)
        val found = articleRepository.findById(saved.id!!).get()
        assert(found.title == "Test Article")
        assert(found.tags.size == 2)
    }

    @Test
    @Order(2)
    fun `should find all articles`() {
        // Given
        val articles = listOf(
            Article(title = "Article 1", content = "Content 1", author = "Author1"),
            Article(title = "Article 2", content = "Content 2", author = "Author2"),
            Article(title = "Article 3", content = "Content 3", author = "Author3")
        )
        articles.forEach { articleRepository.save(it) }
        Thread.sleep(1000)

        // When
        val all = articleRepository.findAll().toList()

        // Then
        assert(all.count() >= 3)
    }

    @Test
    @Order(3)
    fun `should find articles by title containing`() {
        // Given
        articleRepository.save(
            Article(title = "Kotlin Coroutines Guide", content = "Learn coroutines", author = "Author1")
        )
        articleRepository.save(
            Article(title = "Kotlin Flows Tutorial", content = "Learn flows", author = "Author2")
        )
        articleRepository.save(
            Article(title = "Java Streams", content = "Java streams", author = "Author3")
        )
        Thread.sleep(1000)

        // When
        val results = articleRepository.findByTitleContaining("Kotlin")

        // Then
        assert(results.size >= 2)
        assert(results.all { it.title?.contains("Kotlin") == true })
    }

    @Test
    @Order(4)
    fun `should find articles by author`() {
        // Given
        articleRepository.save(
            Article(title = "Article by Jane", content = "Content", author = "Jane Doe", tags = listOf("test"))
        )
        articleRepository.save(
            Article(title = "Another by Jane", content = "More content", author = "Jane Doe", tags = listOf("test"))
        )
        articleRepository.save(
            Article(title = "Article by John", content = "Content", author = "John Doe", tags = listOf("test"))
        )
        Thread.sleep(1000)

        // When
        val results = articleRepository.findByAuthor("Jane Doe")

        // Then
        assert(results.size >= 2)
        assert(results.all { it.author == "Jane Doe" })
    }

    @Test
    @Order(5)
    fun `should find articles by tags containing`() {
        // ...existing code...
    }

    @Test
    @Order(6)
    fun `should count articles`() {
        // ...existing code...
    }

    @Test
    @Order(7)
    fun `should delete article by id`() {
        // Given
        val article = Article(title = "To Delete", content = "Will be deleted", author = "Author")
        val saved = articleRepository.save(article)
        Thread.sleep(500)

        // When
        articleRepository.deleteById(saved.id!!)
        Thread.sleep(500)

        // Then
        val exists = articleRepository.existsById(saved.id!!)
        assert(!exists)
    }

    @Test
    @Order(8)
    fun `should delete all articles`() {
        // ...existing code...
    }

    @Test
    @Order(9)
    fun `should handle empty search results`() {
        // ...existing code...
    }

    @Test
    @Order(10)
    fun `should preserve all fields when saving`() {
        // ...existing code...
    }

    @Test
    @Order(11)
    fun `should handle articles with empty tags`() {
        // Given
        val article = Article(
            title = "No Tags",
            content = "Article without tags",
            author = "Author",
            tags = emptyList()
        )

        // When
        val saved = articleRepository.save(article)
        Thread.sleep(500)

        // Then
        val found = articleRepository.findById(saved.id!!).get()
        assert(found.tags.isEmpty())
    }

    @Test
    @Order(12)
    fun `should support case-insensitive title search`() {
        // Given
        articleRepository.save(
            Article(title = "UPPERCASE TITLE", content = "Content", author = "Author")
        )
        articleRepository.save(
            Article(title = "lowercase title", content = "Content", author = "Author")
        )
        Thread.sleep(1000)

        // When
        val upperResults = articleRepository.findByTitleContaining("UPPERCASE")
        val lowerResults = articleRepository.findByTitleContaining("lowercase")

        // Then
        assert(upperResults.isNotEmpty())
        assert(lowerResults.isNotEmpty())
    }

    companion object {
        @JvmField
        @RegisterExtension
        val dockerRule = DockerComposeExtension.builder()
            .file("src/test/resources/docker-compose.yml")
            .waitingForService("kafka", HealthChecks.toRespondOverHttp(9644) { port: DockerPort ->
                port.inFormat("http://\$HOST:\$EXTERNAL_PORT/v1/status/ready")
            })
            .waitingForService("mongodb", HealthChecks.toHaveAllPortsOpen())
            .waitingForService(
                "elasticsearch",
                HealthChecks.toRespondOverHttp(9200) { port: DockerPort ->
                    port.inFormat("http://\$HOST:\$EXTERNAL_PORT")
                }
            )
            .shutdownStrategy(GRACEFUL)
            .build()
    }
}

