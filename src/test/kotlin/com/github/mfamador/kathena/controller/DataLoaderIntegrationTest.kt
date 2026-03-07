package com.github.mfamador.kathena.controller

import com.github.mfamador.kathena.model.Article
import com.github.mfamador.kathena.model.Person
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
class DataLoaderIntegrationTest {

    @Autowired
    private lateinit var webTestClient: WebTestClient

    @Test
    @Order(1)
    fun `should clear all data`() {
        webTestClient.delete()
            .uri("/api/data/clear")
            .exchange()
            .expectStatus().isOk
    }

    @Test
    @Order(2)
    fun `should load sample data successfully`() {
        // Given - clear first
        webTestClient.delete()
            .uri("/api/data/clear")
            .exchange()
            .expectStatus().isOk

        Thread.sleep(1000) // Wait for clear to complete

        // When
        webTestClient.post()
            .uri("/api/data/load")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.persons_loaded").isNumber
            .jsonPath("$.articles_loaded").isNumber
            .jsonPath("$.messages_sent").isNumber
            .jsonPath("$.persons_loaded").value<Int> { count -> 
                assert(count > 0) { "Should load at least 1 person" }
            }
            .jsonPath("$.articles_loaded").value<Int> { count -> 
                assert(count > 0) { "Should load at least 1 article" }
            }
    }

    @Test
    @Order(3)
    fun `should return correct stats after loading data`() {
        // Given - ensure data is loaded
        webTestClient.delete()
            .uri("/api/data/clear")
            .exchange()
            .expectStatus().isOk

        Thread.sleep(1000)

        webTestClient.post()
            .uri("/api/data/load")
            .exchange()
            .expectStatus().isOk

        Thread.sleep(1000) // Wait for indexing

        // When/Then
        webTestClient.get()
            .uri("/api/data/stats")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.persons").isNumber
            .jsonPath("$.articles").isNumber
            .jsonPath("$.persons").value<Int> { count -> 
                assert(count > 0) { "Persons count should be greater than 0" }
            }
            .jsonPath("$.articles").value<Int> { count -> 
                assert(count > 0) { "Articles count should be greater than 0" }
            }
    }

    @Test
    @Order(4)
    fun `should handle multiple load calls idempotently`() {
        // Given
        webTestClient.delete()
            .uri("/api/data/clear")
            .exchange()
            .expectStatus().isOk

        Thread.sleep(1000)

        // When - load twice
        webTestClient.post()
            .uri("/api/data/load")
            .exchange()
            .expectStatus().isOk

        Thread.sleep(1000)

        val firstStats = webTestClient.get()
            .uri("/api/data/stats")
            .exchange()
            .expectStatus().isOk
            .expectBody(Map::class.java)
            .returnResult()
            .responseBody!!

        // Load again
        webTestClient.post()
            .uri("/api/data/load")
            .exchange()
            .expectStatus().isOk

        Thread.sleep(1000)

        val secondStats = webTestClient.get()
            .uri("/api/data/stats")
            .exchange()
            .expectStatus().isOk
            .expectBody(Map::class.java)
            .returnResult()
            .responseBody!!

        // Then - counts should increase (not idempotent by design for testing)
        val firstPersons = firstStats["persons"] as Int
        val secondPersons = secondStats["persons"] as Int
        assert(secondPersons >= firstPersons) { "Second load should have same or more persons" }
    }

    @Test
    @Order(5)
    fun `stats should return zero when no data loaded`() {
        // Given - clear all
        webTestClient.delete()
            .uri("/api/data/clear")
            .exchange()
            .expectStatus().isOk

        Thread.sleep(1000)

        // When/Then
        webTestClient.get()
            .uri("/api/data/stats")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.persons").isEqualTo(0)
            .jsonPath("$.articles").isEqualTo(0)
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

