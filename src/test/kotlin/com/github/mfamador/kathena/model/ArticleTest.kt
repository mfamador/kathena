package com.github.mfamador.kathena.model

import org.junit.jupiter.api.Test
import java.time.LocalDateTime

class ArticleTest {

    @Test
    fun `should create article with all fields`() {
        // Given/When
        val article = Article(
            id = "article-123",
            title = "Test Article",
            content = "This is test content",
            author = "John Doe",
            tags = listOf("kotlin", "testing", "spring"),
            publishedAt = LocalDateTime.now()
        )

        // Then
        assert(article.id == "article-123")
        assert(article.title == "Test Article")
        assert(article.content == "This is test content")
        assert(article.author == "John Doe")
        assert(article.tags.size == 3)
        assert(article.publishedAt != null)
    }

    @Test
    fun `should create article with default values`() {
        // Given/When
        val article = Article()

        // Then
        assert(article.id == null)
        assert(article.title == null)
        assert(article.content == null)
        assert(article.author == null)
        assert(article.tags.isEmpty()) // Default empty list
        assert(article.publishedAt != null) // Has default value
    }

    @Test
    fun `should create article with minimal fields`() {
        // Given/When
        val article = Article(title = "Minimal Article")

        // Then
        assert(article.title == "Minimal Article")
        assert(article.id == null)
        assert(article.content == null)
        assert(article.tags.isEmpty())
    }

    @Test
    fun `should support data class copy`() {
        // Given
        val original = Article(
            id = "1",
            title = "Original Title",
            content = "Original content",
            author = "Original Author",
            tags = listOf("tag1", "tag2")
        )

        // When
        val modified = original.copy(
            title = "Modified Title",
            tags = listOf("tag1", "tag2", "tag3")
        )

        // Then
        assert(modified.id == "1")
        assert(modified.title == "Modified Title")
        assert(modified.content == "Original content") // Unchanged
        assert(modified.author == "Original Author") // Unchanged
        assert(modified.tags.size == 3)
    }

    @Test
    fun `should support equality comparison`() {
        // Given
        val publishedAt = LocalDateTime.now()
        val article1 = Article(
            id = "1",
            title = "Title",
            content = "Content",
            author = "Author",
            tags = listOf("tag1"),
            publishedAt = publishedAt
        )
        val article2 = Article(
            id = "1",
            title = "Title",
            content = "Content",
            author = "Author",
            tags = listOf("tag1"),
            publishedAt = publishedAt
        )
        val article3 = Article(
            id = "2",
            title = "Different",
            content = "Different",
            author = "Different",
            tags = emptyList(),
            publishedAt = publishedAt
        )

        // Then
        assert(article1 == article2) { "Articles with same data should be equal" }
        assert(article1 != article3) { "Articles with different data should not be equal" }
        assert(article1.hashCode() == article2.hashCode()) { "Equal articles should have same hash code" }
    }

    @Test
    fun `should handle empty tags list`() {
        // Given/When
        val article = Article(
            title = "No Tags",
            content = "Article without tags",
            author = "Author",
            tags = emptyList()
        )

        // Then
        assert(article.tags.isEmpty())
        assert(article.tags.size == 0)
    }

    @Test
    fun `should handle multiple tags`() {
        // Given/When
        val article = Article(
            title = "Multi-tag Article",
            content = "Content",
            author = "Author",
            tags = listOf("kotlin", "spring", "webflux", "reactive", "testing")
        )

        // Then
        assert(article.tags.size == 5)
        assert(article.tags.contains("kotlin"))
        assert(article.tags.contains("reactive"))
        assert(article.tags.containsAll(listOf("spring", "webflux")))
    }

    @Test
    fun `should handle duplicate tags`() {
        // Given/When
        val article = Article(
            title = "Duplicate Tags",
            content = "Content",
            author = "Author",
            tags = listOf("kotlin", "kotlin", "spring")
        )

        // Then
        assert(article.tags.size == 3) // Duplicates allowed in list
        assert(article.tags.count { it == "kotlin" } == 2)
    }

    @Test
    fun `should handle null values properly`() {
        // Given/When
        val article = Article(
            id = "1",
            title = null,
            content = null,
            author = null,
            tags = emptyList()
        )

        // Then
        assert(article.id == "1")
        assert(article.title == null)
        assert(article.content == null)
        assert(article.author == null)
        assert(article.tags.isEmpty())
    }

    @Test
    fun `should convert to string properly`() {
        // Given
        val article = Article(
            id = "1",
            title = "Test",
            content = "Content",
            author = "Author",
            tags = listOf("tag1", "tag2")
        )

        // When
        val string = article.toString()

        // Then
        assert(string.contains("id=1"))
        assert(string.contains("title=Test"))
        assert(string.contains("author=Author"))
        assert(string.contains("tags="))
    }

    @Test
    fun `should preserve timestamp on copy`() {
        // Given
        val timestamp = LocalDateTime.of(2024, 1, 15, 10, 30)
        val original = Article(
            title = "Original",
            content = "Content",
            author = "Author",
            publishedAt = timestamp
        )

        // When
        val copy = original.copy(title = "Modified")

        // Then
        assert(copy.publishedAt == timestamp)
    }
}

