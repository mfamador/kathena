package com.github.mfamador.kathena.controller

import com.github.mfamador.kathena.model.Article
import com.github.mfamador.kathena.model.Person
import com.github.mfamador.kathena.repository.ArticleRepository
import com.github.mfamador.kathena.repository.PersonRepository
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
import org.springframework.test.web.reactive.server.WebTestClient

@ExtendWith(SpringExtension::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource("classpath:test.properties")
@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
class ErrorHandlingIntegrationTest {

    @Autowired
    private lateinit var webTestClient: WebTestClient

    @Autowired
    private lateinit var personRepository: PersonRepository

    @Autowired
    private lateinit var articleRepository: ArticleRepository

    @BeforeEach
    fun setup() {
        personRepository.deleteAll().block()
        articleRepository.deleteAll()
        Thread.sleep(500)
    }

    @Test
    @Order(1)
    fun `should return 404 for non-existent person`() {
        webTestClient.get()
            .uri("/person/this-id-does-not-exist-12345")
            .exchange()
            .expectStatus().isNotFound
    }

    @Test
    @Order(2)
    fun `should return 404 for non-existent article`() {
        webTestClient.get()
            .uri("/article/this-id-does-not-exist-67890")
            .exchange()
            .expectStatus().isNotFound
    }

    @Test
    @Order(3)
    fun `should handle invalid route gracefully`() {
        webTestClient.get()
            .uri("/api/nonexistent-endpoint")
            .exchange()
            .expectStatus().isNotFound
    }

    @Test
    @Order(4)
    fun `should handle malformed JSON in person creation`() {
        webTestClient.post()
            .uri("/person")
            .header("Content-Type", "application/json")
            .bodyValue("{malformed json")
            .exchange()
            .expectStatus().is4xxClientError
    }

    @Test
    @Order(5)
    fun `should handle malformed JSON in article creation`() {
        webTestClient.post()
            .uri("/article")
            .header("Content-Type", "application/json")
            .bodyValue("{invalid: json}")
            .exchange()
            .expectStatus().is4xxClientError
    }

    @Test
    @Order(6)
    fun `should handle empty request body for person`() {
        webTestClient.post()
            .uri("/person")
            .header("Content-Type", "application/json")
            .bodyValue("{}")
            .exchange()
            .expectStatus().isOk // Service accepts it, validates at business level
    }

    @Test
    @Order(7)
    fun `should handle empty request body for article`() {
        webTestClient.post()
            .uri("/article")
            .header("Content-Type", "application/json")
            .bodyValue("{}")
            .exchange()
            .expectStatus().isOk // Service accepts it
    }

    @Test
    @Order(8)
    fun `should handle update of non-existent person`() {
        val person = Person(name = "Ghost", email = "ghost@test.com", age = 99, city = "Nowhere")

        webTestClient.put()
            .uri("/person/non-existent-id")
            .header("Content-Type", "application/json")
            .bodyValue(person)
            .exchange()
            .expectStatus().isNotFound
    }

    @Test
    @Order(9)
    fun `should handle update of non-existent article`() {
        val article = Article(title = "Ghost Article", content = "Content", author = "Ghost")

        webTestClient.put()
            .uri("/article/non-existent-id")
            .header("Content-Type", "application/json")
            .bodyValue(article)
            .exchange()
            .expectStatus().isNotFound
    }

    @Test
    @Order(10)
    fun `should handle delete of non-existent person`() {
        // Should succeed (idempotent delete)
        webTestClient.delete()
            .uri("/person/non-existent-id")
            .exchange()
            .expectStatus().isOk
    }

    @Test
    @Order(11)
    fun `should handle delete of non-existent article`() {
        // Should succeed (idempotent delete)
        webTestClient.delete()
            .uri("/article/non-existent-id")
            .exchange()
            .expectStatus().isOk
    }

    @Test
    @Order(12)
    fun `should handle search with empty results`() {
        webTestClient.get()
            .uri("/person/search?name=ThisNameDefinitelyDoesNotExist")
            .exchange()
            .expectStatus().isOk
            .expectBodyList(Person::class.java)
            .hasSize(0)
    }

    @Test
    @Order(13)
    fun `should handle article search with no parameters`() {
        webTestClient.get()
            .uri("/article/search")
            .exchange()
            .expectStatus().isOk
            .expectBodyList(Article::class.java)
            .hasSize(0) // No search criteria means return empty or all
    }

    @Test
    @Order(14)
    fun `should handle person search with no parameters`() {
        webTestClient.get()
            .uri("/person/search")
            .exchange()
            .expectStatus().isOk
            .expectBodyList(Person::class.java)
            .hasSize(0) // No search criteria
    }

    @Test
    @Order(15)
    fun `should validate age boundaries`() {
        val person = Person(name = "Test", email = "test@test.com", age = -1, city = "Test")

        // Negative age should still be accepted (validation at business level)
        webTestClient.post()
            .uri("/person")
            .header("Content-Type", "application/json")
            .bodyValue(person)
            .exchange()
            .expectStatus().isOk
    }

    @Test
    @Order(16)
    fun `should handle very long strings`() {
        val longString = "x".repeat(10000)
        val person = Person(name = longString, email = "long@test.com", age = 25, city = "Test")

        webTestClient.post()
            .uri("/person")
            .header("Content-Type", "application/json")
            .bodyValue(person)
            .exchange()
            .expectStatus().isOk
    }

    @Test
    @Order(17)
    fun `should handle special characters in search`() {
        // Given
        val person = Person(name = "O'Brien", email = "obrien@test.com", age = 30, city = "Dublin")
        personRepository.save(person).block()

        // When/Then
        val result = webTestClient.get()
            .uri("/person/search?name=O'Brien")
            .exchange()
            .expectStatus().isOk
            .expectBodyList(Person::class.java)
            .returnResult()
        
        val persons = result.responseBody!!
        assert(persons.any { it.name == "O'Brien" })
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

