package com.github.mfamador.kathena.controller

import com.github.mfamador.kathena.model.Article
import com.github.mfamador.kathena.repository.ArticleRepository
import com.palantir.docker.compose.DockerComposeExtension
import com.palantir.docker.compose.configuration.ShutdownStrategy.GRACEFUL
import com.palantir.docker.compose.connection.DockerPort
import com.palantir.docker.compose.connection.waiting.HealthChecks
import org.junit.jupiter.api.*
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.api.extension.RegisterExtension
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.reactive.server.WebTestClient

@ExtendWith(SpringExtension::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource("classpath:test.properties")
@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
class ArticleIntegrationTest {

    @Autowired
    private lateinit var webTestClient: WebTestClient

    @Autowired
    private lateinit var articleRepository: ArticleRepository

    @BeforeEach
    fun setup() {
        // Clean before each test
        articleRepository.deleteAll()
        Thread.sleep(500) // Give Elasticsearch time to process
    }

    @Test
    @Order(1)
    fun `should create a new article`() {
        val article = Article(
            title = "Test Article",
            content = "This is test content about Kotlin and Spring",
            author = "Test Author",
            tags = listOf("kotlin", "spring", "test")
        )

        webTestClient.post()
            .uri("/article")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(article)
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.id").isNotEmpty
            .jsonPath("$.title").isEqualTo("Test Article")
            .jsonPath("$.author").isEqualTo("Test Author")
            .jsonPath("$.tags").isArray
            .jsonPath("$.tags[0]").isEqualTo("kotlin")
    }

    @Test
    @Order(2)
    fun `should get all articles`() {
        // Given
        val articles = listOf(
            Article(title = "Article 1", content = "Content 1", author = "Author1", tags = listOf("tag1")),
            Article(title = "Article 2", content = "Content 2", author = "Author2", tags = listOf("tag2")),
            Article(title = "Article 3", content = "Content 3", author = "Author3", tags = listOf("tag3"))
        )
        articles.forEach { articleRepository.save(it) }
        Thread.sleep(1000) // Wait for Elasticsearch indexing

        // When/Then
        webTestClient.get()
            .uri("/article")
            .exchange()
            .expectStatus().isOk
            .expectBodyList(Article::class.java)
            .hasSize(3)
    }

    @Test
    @Order(3)
    fun `should get article by id`() {
        // Given
        val article = Article(
            title = "Specific Article",
            content = "Specific content",
            author = "Specific Author",
            tags = listOf("specific")
        )
        val saved = articleRepository.save(article)
        Thread.sleep(500)

        // When/Then
        webTestClient.get()
            .uri("/article/${saved.id}")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.title").isEqualTo("Specific Article")
            .jsonPath("$.author").isEqualTo("Specific Author")
    }

    @Test
    @Order(4)
    fun `should return 404 when article not found`() {
        webTestClient.get()
            .uri("/article/nonexistent-id")
            .exchange()
            .expectStatus().isNotFound
    }

    @Test
    @Order(5)
    fun `should update existing article`() {
        // Given
        val article = Article(
            title = "Original Title",
            content = "Original content",
            author = "Original Author",
            tags = listOf("original")
        )
        val saved = articleRepository.save(article)
        Thread.sleep(500)

        // When
        val updated = saved.copy(
            title = "Updated Title",
            content = "Updated content",
            tags = listOf("updated", "modified")
        )

        // Then
        webTestClient.put()
            .uri("/article/${saved.id}")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(updated)
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.title").isEqualTo("Updated Title")
            .jsonPath("$.content").isEqualTo("Updated content")
            .jsonPath("$.tags[0]").isEqualTo("updated")
    }

    @Test
    @Order(6)
    fun `should delete article by id`() {
        // Given
        val article = Article(
            title = "To Delete",
            content = "Will be deleted",
            author = "Delete Author",
            tags = emptyList()
        )
        val saved = articleRepository.save(article)
        Thread.sleep(500)

        // When
        webTestClient.delete()
            .uri("/article/${saved.id}")
            .exchange()
            .expectStatus().isOk

        // Then - verify it's deleted
        Thread.sleep(500)
        val exists = articleRepository.existsById(saved.id!!)
        assert(!exists) { "Article should have been deleted" }
    }

    @Test
    @Order(7)
    fun `should search articles by title`() {
        // Given
        articleRepository.save(
            Article(
                title = "Kotlin Coroutines Guide",
                content = "Learn about Kotlin coroutines",
                author = "Kotlin Expert",
                tags = listOf("kotlin", "coroutines")
            )
        )
        articleRepository.save(
            Article(
                title = "Spring Boot Tutorial",
                content = "Learn Spring Boot",
                author = "Spring Expert",
                tags = listOf("spring", "java")
            )
        )
        Thread.sleep(1000) // Wait for indexing

        // When/Then
        val result = webTestClient.get()
            .uri("/article/search?title=Kotlin")
            .exchange()
            .expectStatus().isOk
            .expectBodyList(Article::class.java)
            .returnResult()
        
        val articles = result.responseBody!!
        assert(articles.isNotEmpty()) { "Should find at least 1 article with 'Kotlin' in title" }
        assert(articles.any { it.title?.contains("Kotlin") == true })
    }

    @Test
    @Order(8)
    fun `should search articles by content`() {
        // Given
        articleRepository.save(
            Article(
                title = "Reactive Programming",
                content = "WebFlux enables reactive programming with Spring",
                author = "Reactive Expert",
                tags = listOf("reactive", "webflux")
            )
        )
        Thread.sleep(1000)

        // When/Then
        val result = webTestClient.get()
            .uri("/article/search?content=reactive")
            .exchange()
            .expectStatus().isOk
            .expectBodyList(Article::class.java)
            .returnResult()
        
        val articles = result.responseBody!!
        assert(articles.isNotEmpty()) { "Should find articles with 'reactive' in content" }
    }

    @Test
    @Order(9)
    fun `should search articles by author`() {
        // Given
        articleRepository.save(
            Article(
                title = "Author Test",
                content = "Content by specific author",
                author = "Jane Doe",
                tags = listOf("test")
            )
        )
        Thread.sleep(1000)

        // When/Then
        webTestClient.get()
            .uri("/article/search?author=Jane Doe")
            .exchange()
            .expectStatus().isOk
            .expectBodyList(Article::class.java)
            .hasSize(1)
    }

    @Test
    @Order(10)
    fun `should search articles by tags`() {
        // Given
        articleRepository.save(
            Article(
                title = "Tagged Article",
                content = "Content with specific tags",
                author = "Tag Author",
                tags = listOf("kotlin", "reactive", "testing")
            )
        )
        Thread.sleep(1000)

        // When/Then
        val result = webTestClient.get()
            .uri("/article/search?tags=reactive")
            .exchange()
            .expectStatus().isOk
            .expectBodyList(Article::class.java)
            .returnResult()
        
        val articles = result.responseBody!!
        assert(articles.isNotEmpty()) { "Should find articles tagged with 'reactive'" }
    }

    @Test
    @Order(11)
    fun `should return empty list when no articles match search`() {
        webTestClient.get()
            .uri("/article/search?title=NonExistentArticle")
            .exchange()
            .expectStatus().isOk
            .expectBodyList(Article::class.java)
            .hasSize(0)
    }

    @Test
    @Order(12)
    fun `should handle multiple search parameters`() {
        // Given
        articleRepository.save(
            Article(
                title = "Kotlin WebFlux",
                content = "Building reactive apps with Kotlin and WebFlux",
                author = "Kotlin Master",
                tags = listOf("kotlin", "webflux", "reactive")
            )
        )
        Thread.sleep(1000)

        // When/Then - search with multiple criteria
        val result = webTestClient.get()
            .uri("/article/search?title=Kotlin&author=Kotlin Master")
            .exchange()
            .expectStatus().isOk
            .expectBodyList(Article::class.java)
            .returnResult()
        
        val articles = result.responseBody!!
        assert(articles.isNotEmpty())
        assert(articles.all { it.author == "Kotlin Master" })
    }

    companion object {
        @JvmField
        @RegisterExtension
        val dockerRule: DockerComposeExtension? = DockerComposeExtension.builder()
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

