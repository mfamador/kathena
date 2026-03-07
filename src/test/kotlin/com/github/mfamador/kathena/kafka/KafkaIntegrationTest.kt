package com.github.mfamador.kathena.kafka

import com.github.mfamador.kathena.messaging.EventProducer
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

@ExtendWith(SpringExtension::class)
@SpringBootTest
@TestPropertySource("classpath:test.properties")
@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
class KafkaIntegrationTest {

    @Autowired
    private lateinit var eventProducer: EventProducer

    @Test
    @Order(1)
    fun `should produce message to Kafka topic`() {
        // Given
        val message = "Test message from producer"

        // When
        eventProducer.send(message)

        // Then - wait a bit for the message to be consumed
        Thread.sleep(2000)
        
        // The consumer should have logged the message
        // In a real scenario, you'd use a test listener or mock
    }

    @Test
    @Order(2)
    fun `should handle multiple messages`() {
        // Given
        val messages = listOf(
            "Message 1: User registration",
            "Message 2: Order placed",
            "Message 3: Payment processed"
        )

        // When
        messages.forEach { eventProducer.send(it) }

        // Then
        Thread.sleep(3000) // Wait for processing
        // Messages should be consumed without errors
    }

    @Test
    @Order(3)
    fun `should handle empty messages gracefully`() {
        // When/Then - should not throw exception
        assertDoesNotThrow {
            eventProducer.send("")
        }
    }

    @Test
    @Order(4)
    fun `should handle special characters in messages`() {
        // Given
        val specialMessage = "Test: Special chars → ñ, ü, 中文, 😀"

        // When/Then - should not throw exception
        assertDoesNotThrow {
            eventProducer.send(specialMessage)
            Thread.sleep(1000)
        }
    }

    @Test
    @Order(5)
    fun `should handle large messages`() {
        // Given
        val largeMessage = "x".repeat(10000) // 10KB message

        // When/Then
        assertDoesNotThrow {
            eventProducer.send(largeMessage)
            Thread.sleep(1000)
        }
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

