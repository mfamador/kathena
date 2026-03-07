package com.github.mfamador.kathena.controller

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
class ActuatorEndpointsTest {

    @Autowired
    private lateinit var webTestClient: WebTestClient

    @Test
    fun `health endpoint should return UP status`() {
        webTestClient.get()
            .uri("/actuator/health")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.status").isEqualTo("UP")
    }

    @Test
    fun `info endpoint should return application info`() {
        webTestClient.get()
            .uri("/actuator/info")
            .exchange()
            .expectStatus().isOk
    }

    @Test
    fun `metrics endpoint should be accessible`() {
        webTestClient.get()
            .uri("/actuator/metrics")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.names").isArray
    }

    @Test
    fun `prometheus endpoint should return metrics`() {
        webTestClient.get()
            .uri("/actuator/prometheus")
            .exchange()
            .expectStatus().isOk
            .expectBody(String::class.java)
            .consumeWith { response ->
                val body = response.responseBody!!
                assert(body.contains("jvm_")) { "Should contain JVM metrics" }
            }
    }

    @Test
    fun `should return specific metric details`() {
        webTestClient.get()
            .uri("/actuator/metrics/jvm.memory.used")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.name").isEqualTo("jvm.memory.used")
            .jsonPath("$.measurements").isArray
    }

    @Test
    fun `env endpoint should be accessible`() {
        webTestClient.get()
            .uri("/actuator/env")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.propertySources").isArray
    }

    @Test
    fun `loggers endpoint should return logger configurations`() {
        webTestClient.get()
            .uri("/actuator/loggers")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.loggers").exists()
    }

    @Test
    fun `should get specific logger configuration`() {
        webTestClient.get()
            .uri("/actuator/loggers/com.github.mfamador.kathena")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.configuredLevel").exists()
    }

    @Test
    fun `beans endpoint should list all beans`() {
        webTestClient.get()
            .uri("/actuator/beans")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.contexts").exists()
    }

    @Test
    fun `conditions endpoint should show auto-configuration report`() {
        webTestClient.get()
            .uri("/actuator/conditions")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.contexts").exists()
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

