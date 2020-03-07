package com.github.mfamador.kathena

import com.palantir.docker.compose.DockerComposeExtension
import com.palantir.docker.compose.connection.waiting.HealthChecks
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.api.extension.RegisterExtension
import org.slf4j.LoggerFactory
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit.jupiter.SpringExtension

@ExtendWith(SpringExtension::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ApplicationTests {
    private val log = LoggerFactory.getLogger(javaClass)

    @BeforeEach
    internal fun beforeEach() = log.info("beforeEach called")

    @AfterEach
    internal fun afterEach() = log.info("afterEach called")

    @Test
    fun contextLoads() {
        log.debug("contextLoads")
    }

    companion object {
        @JvmField
        @RegisterExtension
        val dockerRule = DockerComposeExtension.builder()
            .file("src/test/resources/docker-compose.yml")
            .waitingForService("kafka", HealthChecks.toHaveAllPortsOpen())
            .build()
    }

}
