package com.github.mfamador.kathena.repository

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
import reactor.test.StepVerifier

@ExtendWith(SpringExtension::class)
@SpringBootTest
@TestPropertySource("classpath:test.properties")
@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
class PersonRepositoryTest {

    @Autowired
    private lateinit var personRepository: PersonRepository

    @BeforeEach
    fun setup() {
        personRepository.deleteAll().block()
    }

    @Test
    @Order(1)
    fun `should save and retrieve person`() {
        // Given
        val person = Person(name = "Test User", email = "test@example.com", age = 25, city = "Boston")

        // When
        val saved = personRepository.save(person).block()!!

        // Then
        assert(saved.id != null)
        assert(saved.name == "Test User")
        assert(saved.email == "test@example.com")

        // Retrieve
        val found = personRepository.findById(saved.id!!).block()!!
        assert(found.name == "Test User")
    }

    @Test
    @Order(2)
    fun `should find persons by name containing`() {
        // Given
        personRepository.saveAll(
            listOf(
                Person(name = "Alice Anderson", email = "alice@test.com", age = 25, city = "NYC"),
                Person(name = "Alice Baker", email = "alice2@test.com", age = 30, city = "LA"),
                Person(name = "Bob Carter", email = "bob@test.com", age = 35, city = "SF")
            )
        ).blockLast()

        // When/Then
        StepVerifier.create(personRepository.findByNameContainingIgnoreCase("Alice"))
            .expectNextCount(2)
            .verifyComplete()
    }

    @Test
    @Order(3)
    fun `should find person by name containing`() {
        // Given
        val person = Person(name = "Alice", email = "alice@test.com", age = 25, city = "Portland")
        personRepository.save(person).block()

        // When/Then
        StepVerifier.create(personRepository.findByNameContainingIgnoreCase("Alice"))
            .expectNextCount(1)
            .verifyComplete()
    }

    @Test
    @Order(4)
    fun `should find persons by city`() {
        // Given
        personRepository.saveAll(
            listOf(
                Person(name = "User1", email = "user1@test.com", age = 25, city = "Portland"),
                Person(name = "User2", email = "user2@test.com", age = 30, city = "Portland"),
                Person(name = "User3", email = "user3@test.com", age = 35, city = "Seattle")
            )
        ).blockLast()

        // When/Then
        StepVerifier.create(personRepository.findByCity("Portland"))
            .expectNextCount(2)
            .verifyComplete()
    }

    @Test
    @Order(5)
    fun `should find persons by age greater than`() {
        // Given
        personRepository.saveAll(
            listOf(
                Person(name = "Young1", email = "young1@test.com", age = 20, city = "Boston"),
                Person(name = "Young2", email = "young2@test.com", age = 25, city = "NYC"),
                Person(name = "Old1", email = "old1@test.com", age = 50, city = "LA")
            )
        ).blockLast()

        // When/Then
        StepVerifier.create(personRepository.findByAgeGreaterThan(30))
            .expectNextCount(1) // Only Old1 is > 30
            .verifyComplete()
    }

    @Test
    @Order(6)
    fun `should count all persons`() {
        // Given
        personRepository.saveAll(
            listOf(
                Person(name = "P1", email = "p1@test.com", age = 25, city = "NYC"),
                Person(name = "P2", email = "p2@test.com", age = 30, city = "LA"),
                Person(name = "P3", email = "p3@test.com", age = 35, city = "SF")
            )
        ).blockLast()

        // When/Then
        StepVerifier.create(personRepository.count())
            .expectNext(3L)
            .verifyComplete()
    }

    @Test
    @Order(7)
    fun `should delete person by id`() {
        // Given
        val person = Person(name = "To Delete", email = "delete@test.com", age = 30, city = "Austin")
        val saved = personRepository.save(person).block()!!

        // When
        personRepository.deleteById(saved.id!!).block()

        // Then
        StepVerifier.create(personRepository.findById(saved.id!!))
            .expectNextCount(0)
            .verifyComplete()
    }

    @Test
    @Order(8)
    fun `should delete all persons`() {
        // Given
        personRepository.saveAll(
            listOf(
                Person(name = "P1", email = "p1@test.com", age = 25, city = "NYC"),
                Person(name = "P2", email = "p2@test.com", age = 30, city = "LA")
            )
        ).blockLast()

        // When
        personRepository.deleteAll().block()

        // Then
        StepVerifier.create(personRepository.count())
            .expectNext(0L)
            .verifyComplete()
    }

    @Test
    @Order(9)
    fun `should return empty when searching for non-existent name`() {
        // When/Then
        StepVerifier.create(personRepository.findByNameContainingIgnoreCase("NonExistent"))
            .expectNextCount(0)
            .verifyComplete()
    }

    @Test
    @Order(10)
    fun `should handle special characters in names`() {
        // Given
        val person = Person(
            name = "O'Brien-Müller",
            email = "special@test.com",
            age = 30,
            city = "Zürich"
        )
        val saved = personRepository.save(person).block()!!

        // When/Then
        StepVerifier.create(personRepository.findById(saved.id!!))
            .expectNextMatches { it.name == "O'Brien-Müller" && it.city == "Zürich" }
            .verifyComplete()
    }

    @Test
    @Order(11)
    fun `should preserve creation timestamp`() {
        // Given
        val person = Person(name = "Timestamped", email = "time@test.com", age = 25, city = "Denver")

        // When
        val saved = personRepository.save(person).block()!!

        // Then
        assert(saved.createdAt != null)
        val retrieved = personRepository.findById(saved.id!!).block()!!
        assert(retrieved.createdAt == saved.createdAt)
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

