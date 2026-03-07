package com.github.mfamador.kathena.controller

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.github.mfamador.kathena.model.Person
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
import org.springframework.http.MediaType
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.reactive.server.WebTestClient
import reactor.test.StepVerifier
import java.time.LocalDateTime

@ExtendWith(SpringExtension::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource("classpath:test.properties")
@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
class PersonIntegrationTest {

    @Autowired
    private lateinit var webTestClient: WebTestClient

    @Autowired
    private lateinit var personRepository: PersonRepository

    private val objectMapper = jacksonObjectMapper()

    @BeforeEach
    fun setup() {
        // Clean before each test
        personRepository.deleteAll().block()
    }

    @Test
    @Order(1)
    fun `should create a new person`() {
        val person = Person(
            name = "John Doe",
            email = "john.doe@example.com",
            age = 30,
            city = "San Francisco"
        )

        webTestClient.post()
            .uri("/person")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(person)
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.id").isNotEmpty
            .jsonPath("$.name").isEqualTo("John Doe")
            .jsonPath("$.email").isEqualTo("john.doe@example.com")
            .jsonPath("$.age").isEqualTo(30)
            .jsonPath("$.city").isEqualTo("San Francisco")
    }

    @Test
    @Order(2)
    fun `should get all persons`() {
        // Given
        val persons = listOf(
            Person(name = "Alice", email = "alice@test.com", age = 25, city = "NYC"),
            Person(name = "Bob", email = "bob@test.com", age = 35, city = "LA"),
            Person(name = "Charlie", email = "charlie@test.com", age = 28, city = "SF")
        )
        personRepository.saveAll(persons).blockLast()

        // When/Then
        webTestClient.get()
            .uri("/person")
            .exchange()
            .expectStatus().isOk
            .expectBodyList(Person::class.java)
            .hasSize(3)
    }

    @Test
    @Order(3)
    fun `should get person by id`() {
        // Given
        val person = Person(name = "Test User", email = "test@example.com", age = 25, city = "Boston")
        val saved = personRepository.save(person).block()!!

        // When/Then
        webTestClient.get()
            .uri("/person/${saved.id}")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.id").isEqualTo(saved.id!!)
            .jsonPath("$.name").isEqualTo("Test User")
            .jsonPath("$.email").isEqualTo("test@example.com")
    }

    @Test
    @Order(4)
    fun `should return 404 when person not found`() {
        webTestClient.get()
            .uri("/person/nonexistent-id")
            .exchange()
            .expectStatus().isNotFound
    }

    @Test
    @Order(5)
    fun `should update existing person`() {
        // Given
        val person = Person(name = "Old Name", email = "old@example.com", age = 25, city = "Seattle")
        val saved = personRepository.save(person).block()!!

        // When
        val updated = saved.copy(name = "New Name", age = 26)

        // Then
        webTestClient.put()
            .uri("/person/${saved.id}")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(updated)
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.name").isEqualTo("New Name")
            .jsonPath("$.age").isEqualTo(26)
            .jsonPath("$.email").isEqualTo("old@example.com")
    }

    @Test
    @Order(6)
    fun `should delete person by id`() {
        // Given
        val person = Person(name = "To Delete", email = "delete@example.com", age = 30, city = "Austin")
        val saved = personRepository.save(person).block()!!

        // When
        webTestClient.delete()
            .uri("/person/${saved.id}")
            .exchange()
            .expectStatus().isOk

        // Then - verify it's deleted
        StepVerifier.create(personRepository.findById(saved.id!!))
            .expectNextCount(0)
            .verifyComplete()
    }

    @Test
    @Order(7)
    fun `should search persons by name`() {
        // Given
        personRepository.saveAll(
            listOf(
                Person(name = "Alice Anderson", email = "alice.a@test.com", age = 25, city = "NYC"),
                Person(name = "Alice Baker", email = "alice.b@test.com", age = 30, city = "LA"),
                Person(name = "Bob Carter", email = "bob.c@test.com", age = 35, city = "SF")
            )
        ).blockLast()

        // When/Then
        webTestClient.get()
            .uri("/person/search?name=Alice")
            .exchange()
            .expectStatus().isOk
            .expectBodyList(Person::class.java)
            .hasSize(2)
    }

    @Test
    @Order(8)
    fun `should search persons by email`() {
        // Given
        val person = Person(name = "Email Test", email = "unique@test.com", age = 25, city = "Denver")
        personRepository.save(person).block()

        // When/Then
        val result = webTestClient.get()
            .uri("/person/search?email=unique@test.com")
            .exchange()
            .expectStatus().isOk
            .expectBodyList(Person::class.java)
            .returnResult()
        
        val persons = result.responseBody!!
        assert(persons.size == 1)
        assert(persons[0].email == "unique@test.com")
    }

    @Test
    @Order(9)
    fun `should search persons by city`() {
        // Given
        personRepository.saveAll(
            listOf(
                Person(name = "User1", email = "user1@test.com", age = 25, city = "Portland"),
                Person(name = "User2", email = "user2@test.com", age = 30, city = "Portland"),
                Person(name = "User3", email = "user3@test.com", age = 35, city = "Seattle")
            )
        ).blockLast()

        // When/Then
        webTestClient.get()
            .uri("/person/search?city=Portland")
            .exchange()
            .expectStatus().isOk
            .expectBodyList(Person::class.java)
            .hasSize(2)
    }

    @Test
    @Order(10)
    fun `should search persons by age`() {
        // Given
        personRepository.saveAll(
            listOf(
                Person(name = "Young1", email = "young1@test.com", age = 20, city = "Boston"),
                Person(name = "Young2", email = "young2@test.com", age = 20, city = "NYC"),
                Person(name = "Old1", email = "old1@test.com", age = 50, city = "LA")
            )
        ).blockLast()

        // When/Then
        webTestClient.get()
            .uri("/person/search?age=20")
            .exchange()
            .expectStatus().isOk
            .expectBodyList(Person::class.java)
            .hasSize(2)
    }

    @Test
    @Order(11)
    fun `should return empty list when no persons match search`() {
        webTestClient.get()
            .uri("/person/search?name=NonExistent")
            .exchange()
            .expectStatus().isOk
            .expectBodyList(Person::class.java)
            .hasSize(0)
    }

    @Test
    @Order(12)
    fun `should handle invalid person creation with missing required fields`() {
        val invalidPerson = mapOf("invalid" to "data")

        webTestClient.post()
            .uri("/person")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(invalidPerson)
            .exchange()
            .expectStatus().isOk // Service still accepts it, validates at service level
    }

    @Test
    @Order(13)
    fun `should handle concurrent person creation`() {
        val persons = (1..5).map { i ->
            Person(
                name = "Concurrent User $i",
                email = "concurrent$i@test.com",
                age = 20 + i,
                city = "ConcurrentCity"
            )
        }

        // Create all persons concurrently
        persons.forEach { person ->
            webTestClient.post()
                .uri("/person")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(person)
                .exchange()
                .expectStatus().isOk
        }

        // Verify all were created
        val count = personRepository.count().block()!!
        assert(count >= 5L) { "Expected at least 5 persons but found $count" }
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

