package com.github.mfamador.kathena.model

import org.junit.jupiter.api.Test
import java.time.LocalDateTime

class PersonTest {

    @Test
    fun `should create person with all fields`() {
        // Given/When
        val person = Person(
            id = "123",
            name = "John Doe",
            email = "john@example.com",
            age = 30,
            city = "San Francisco",
            createdAt = LocalDateTime.now()
        )

        // Then
        assert(person.id == "123")
        assert(person.name == "John Doe")
        assert(person.email == "john@example.com")
        assert(person.age == 30)
        assert(person.city == "San Francisco")
        assert(person.createdAt != null)
    }

    @Test
    fun `should create person with default values`() {
        // Given/When
        val person = Person()

        // Then
        assert(person.id == null)
        assert(person.name == null)
        assert(person.email == null)
        assert(person.age == null)
        assert(person.city == null)
        assert(person.createdAt != null) // Has default value
    }

    @Test
    fun `should create person with minimal fields`() {
        // Given/When
        val person = Person(name = "Jane")

        // Then
        assert(person.name == "Jane")
        assert(person.id == null)
        assert(person.email == null)
    }

    @Test
    fun `should support data class copy`() {
        // Given
        val original = Person(
            id = "1",
            name = "Original Name",
            email = "original@test.com",
            age = 25,
            city = "NYC"
        )

        // When
        val modified = original.copy(name = "Modified Name", age = 26)

        // Then
        assert(modified.id == "1")
        assert(modified.name == "Modified Name")
        assert(modified.age == 26)
        assert(modified.email == "original@test.com") // Unchanged
        assert(modified.city == "NYC") // Unchanged
    }

    @Test
    fun `should support equality comparison`() {
        // Given
        val createdAt = LocalDateTime.now()
        val person1 = Person(id = "1", name = "John", email = "john@test.com", age = 30, city = "SF", createdAt = createdAt)
        val person2 = Person(id = "1", name = "John", email = "john@test.com", age = 30, city = "SF", createdAt = createdAt)
        val person3 = Person(id = "2", name = "Jane", email = "jane@test.com", age = 25, city = "NYC", createdAt = createdAt)

        // Then
        assert(person1 == person2) { "Persons with same data should be equal" }
        assert(person1 != person3) { "Persons with different data should not be equal" }
        assert(person1.hashCode() == person2.hashCode()) { "Equal persons should have same hash code" }
    }

    @Test
    fun `should handle null values properly`() {
        // Given/When
        val person = Person(
            id = "1",
            name = null,
            email = null,
            age = null,
            city = null
        )

        // Then
        assert(person.id == "1")
        assert(person.name == null)
        assert(person.email == null)
        assert(person.age == null)
        assert(person.city == null)
    }

    @Test
    fun `should convert to string properly`() {
        // Given
        val person = Person(id = "1", name = "Test", email = "test@test.com", age = 25, city = "Boston")

        // When
        val string = person.toString()

        // Then
        assert(string.contains("id=1"))
        assert(string.contains("name=Test"))
        assert(string.contains("email=test@test.com"))
        assert(string.contains("age=25"))
        assert(string.contains("city=Boston"))
    }
}

