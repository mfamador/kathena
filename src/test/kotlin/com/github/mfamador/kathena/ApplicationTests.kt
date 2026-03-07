package com.github.mfamador.kathena

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.palantir.docker.compose.DockerComposeExtension
import com.palantir.docker.compose.configuration.ShutdownStrategy.GRACEFUL
import com.palantir.docker.compose.connection.DockerPort
import com.palantir.docker.compose.connection.waiting.HealthChecks
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.api.extension.RegisterExtension
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.junit.jupiter.SpringExtension

@ExtendWith(SpringExtension::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource("classpath:test.properties")
class ApplicationTests {
    private val log = LoggerFactory.getLogger(javaClass)
    private val maxLoadAttempts = 5
    private val loadBackoffMs = 2000L

    @Autowired
    private lateinit var webTestClient: WebTestClient

    private val objectMapper = jacksonObjectMapper()

    @BeforeEach
    internal fun beforeEach() = log.info("beforeEach called")

    @AfterEach
    internal fun afterEach() = log.info("afterEach called")

    @Test
    fun contextLoads() {
        log.debug("contextLoads")
        webTestClient.get()
            .uri("/actuator/health")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.status").isNotEmpty
    }

    @Test
    fun loadSampleDataAndStats() {
        webTestClient.delete()
            .uri("/api/data/clear")
            .exchange()
            .expectStatus().isOk

        val loadResponse = postUntilOk("/api/data/load")
        val json = objectMapper.readTree(loadResponse)
        require(json.hasNonNull("persons_loaded")) { "missing persons_loaded in load response" }
        require(json.hasNonNull("articles_loaded")) { "missing articles_loaded in load response" }

        webTestClient.get()
            .uri("/api/data/stats")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.persons").isNumber
            .jsonPath("$.articles").isNumber
    }

    private fun postUntilOk(path: String): String {
        var attempts = 0
        var lastResult: org.springframework.test.web.reactive.server.EntityExchangeResult<String>? = null
        while (attempts < maxLoadAttempts) {
            val result = webTestClient.post()
                .uri(path)
                .exchange()
                .expectBody(String::class.java)
                .returnResult()
            lastResult = result
            if (result.status.is2xxSuccessful) {
                return result.responseBody ?: ""
            }
            if (!result.status.is5xxServerError) {
                break
            }
            Thread.sleep(loadBackoffMs)
            attempts += 1
        }
        val body = lastResult?.responseBody ?: ""
        Assertions.fail<Any>("${path} failed with ${lastResult?.status}. Body: $body")
        return ""
    }

    companion object {
        @JvmField
        @RegisterExtension
        val dockerRule = DockerComposeExtension.builder()
            .file("src/test/resources/docker-compose.yml")
            .waitingForService("kafka", HealthChecks.toRespondOverHttp(9644) { port: DockerPort ->
                port.inFormat("http://${'$'}HOST:${'$'}EXTERNAL_PORT/v1/status/ready")
            })
            .waitingForService("mongodb", HealthChecks.toHaveAllPortsOpen())
            .waitingForService(
                "elasticsearch",
                HealthChecks.toRespondOverHttp(9200) { port: DockerPort ->
                    port.inFormat("http://${'$'}HOST:${'$'}EXTERNAL_PORT")
                }
            )
            .shutdownStrategy(GRACEFUL)
            .build()
    }
}
