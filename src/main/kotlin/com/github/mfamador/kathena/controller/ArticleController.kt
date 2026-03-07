package com.github.mfamador.kathena.controller

import com.github.mfamador.kathena.model.Article
import com.github.mfamador.kathena.service.ArticleService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/article")
@Tag(name = "Article", description = "Article management APIs (Elasticsearch)")
class ArticleController(private val articleService: ArticleService) {

    @GetMapping
    @Operation(
        summary = "Get all articles",
        description = "Retrieve all articles from Elasticsearch",
        responses = [ApiResponse(responseCode = "200", description = "Success")]
    )
    fun getAll(): Flux<Article> = articleService.getAll()

    @GetMapping("/{id}")
    @Operation(
        summary = "Get article by ID",
        description = "Retrieve a specific article by its ID",
        responses = [
            ApiResponse(responseCode = "200", description = "Success"),
            ApiResponse(responseCode = "404", description = "Article not found")
        ]
    )
    fun get(
        @Parameter(description = "Article ID")
        @PathVariable id: String
    ): Mono<Article> = articleService.get(id)

    @GetMapping("/count")
    @Operation(
        summary = "Count all articles",
        description = "Get the total number of articles in Elasticsearch",
        responses = [ApiResponse(responseCode = "200", description = "Success")]
    )
    fun count(): Mono<Long> = articleService.count()

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
        summary = "Create a new article",
        description = "Add a new article to Elasticsearch",
        responses = [ApiResponse(responseCode = "201", description = "Article created successfully")]
    )
    fun add(@RequestBody article: Article): Mono<Article> = articleService.save(article)

    @PostMapping("/batch")
    @Operation(
        summary = "Create multiple articles",
        description = "Add multiple articles to Elasticsearch in a single batch operation",
        responses = [ApiResponse(responseCode = "200", description = "Articles created successfully")]
    )
    fun addBatch(@RequestBody articles: List<Article>): Flux<Article> = articleService.saveAll(articles)

    @PutMapping("/{id}")
    @Operation(
        summary = "Update article",
        description = "Update an existing article by ID",
        responses = [
            ApiResponse(responseCode = "200", description = "Article updated successfully"),
            ApiResponse(responseCode = "404", description = "Article not found")
        ]
    )
    fun update(
        @Parameter(description = "Article ID")
        @PathVariable id: String,
        @RequestBody article: Article
    ): Mono<Article> = articleService.save(article.apply { this.id = id })

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(
        summary = "Delete article",
        description = "Delete an article by ID",
        responses = [
            ApiResponse(responseCode = "204", description = "Article deleted successfully"),
            ApiResponse(responseCode = "404", description = "Article not found")
        ]
    )
    fun delete(
        @Parameter(description = "Article ID")
        @PathVariable id: String
    ): Mono<Void> = articleService.delete(id)

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(
        summary = "Delete all articles",
        description = "Delete all articles from Elasticsearch (use with caution)",
        responses = [ApiResponse(responseCode = "204", description = "All articles deleted")]
    )
    fun deleteAll(): Mono<Void> = articleService.deleteAll()

    @GetMapping("/search")
    @Operation(
        summary = "Search articles by title",
        description = "Full-text search for articles by title",
        responses = [ApiResponse(responseCode = "200", description = "Success")]
    )
    fun searchByTitle(
        @Parameter(description = "Title keywords to search for", example = "Kotlin")
        @RequestParam(required = false, defaultValue = "") title: String
    ): Flux<Article> = articleService.searchByTitle(title)

    @GetMapping("/author/{author}")
    @Operation(
        summary = "Find articles by author",
        description = "Retrieve all articles written by a specific author",
        responses = [ApiResponse(responseCode = "200", description = "Success")]
    )
    fun findByAuthor(
        @Parameter(description = "Author name", example = "Alice Johnson")
        @PathVariable author: String
    ): Flux<Article> = articleService.findByAuthor(author)

    @GetMapping("/tag/{tag}")
    @Operation(
        summary = "Find articles by tag",
        description = "Retrieve all articles containing a specific tag",
        responses = [ApiResponse(responseCode = "200", description = "Success")]
    )
    fun findByTag(
        @Parameter(description = "Tag name", example = "kotlin")
        @PathVariable tag: String
    ): Flux<Article> = articleService.findByTag(tag)
}

