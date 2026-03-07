package com.github.mfamador.kathena.controller

import com.github.mfamador.kathena.model.Article
import com.github.mfamador.kathena.service.ArticleService
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.util.UriComponentsBuilder
import reactor.core.publisher.Mono

@Component
class ArticleHandler(private val articleService: ArticleService) {

    fun getAll(request: ServerRequest): Mono<ServerResponse> =
        ServerResponse.ok().contentType(MediaType.APPLICATION_JSON)
            .body(BodyInserters.fromPublisher(articleService.getAll(), Article::class.java))

    fun count(request: ServerRequest): Mono<ServerResponse> =
        ServerResponse.ok().contentType(MediaType.APPLICATION_JSON)
            .body(BodyInserters.fromPublisher(articleService.count(), Long::class.java))

    fun get(request: ServerRequest): Mono<ServerResponse> =
        articleService.get(getId(request))
            .flatMap { ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).bodyValue(it) }
            .switchIfEmpty(ServerResponse.notFound().build())

    fun add(request: ServerRequest): Mono<ServerResponse> =
        ServerResponse.created(UriComponentsBuilder.fromPath(ARTICLE_ENDPOINT).build().toUri())
            .contentType(MediaType.APPLICATION_JSON)
            .body(
                BodyInserters.fromPublisher(
                    request.bodyToMono(Article::class.java)
                        .flatMap { articleService.save(it) }, Article::class.java
                )
            )

    fun addBatch(request: ServerRequest): Mono<ServerResponse> =
        ServerResponse.ok()
            .contentType(MediaType.APPLICATION_JSON)
            .body(
                BodyInserters.fromPublisher(
                    request.bodyToFlux(Article::class.java)
                        .collectList()
                        .flatMapMany { articleService.saveAll(it) },
                    Article::class.java
                )
            )

    fun update(request: ServerRequest): Mono<ServerResponse> {
        val id = getId(request)
        return articleService.get(id)
            .flatMap {
                ServerResponse.ok().contentType(MediaType.APPLICATION_JSON)
                    .body(
                        BodyInserters.fromPublisher(
                            request.bodyToMono(Article::class.java)
                                .map { body -> body.apply { this.id = id } }
                                .flatMap { articleService.save(it) }, Article::class.java
                        )
                    )
            }
            .switchIfEmpty(ServerResponse.notFound().build())
    }

    fun delete(request: ServerRequest): Mono<ServerResponse> {
        val id = getId(request)
        return articleService.get(id)
            .flatMap { ServerResponse.noContent().build(articleService.delete(id)) }
            .switchIfEmpty(ServerResponse.notFound().build())
    }

    fun deleteAll(request: ServerRequest): Mono<ServerResponse> =
        ServerResponse.noContent().build(articleService.deleteAll())

    // Search handlers
    fun searchByTitle(request: ServerRequest): Mono<ServerResponse> {
        val title = request.queryParam("title").orElse("")
        return ServerResponse.ok().contentType(MediaType.APPLICATION_JSON)
            .body(BodyInserters.fromPublisher(articleService.searchByTitle(title), Article::class.java))
    }

    fun findByAuthor(request: ServerRequest): Mono<ServerResponse> {
        val author = request.pathVariable("author")
        return ServerResponse.ok().contentType(MediaType.APPLICATION_JSON)
            .body(BodyInserters.fromPublisher(articleService.findByAuthor(author), Article::class.java))
    }

    fun findByTag(request: ServerRequest): Mono<ServerResponse> {
        val tag = request.pathVariable("tag")
        return ServerResponse.ok().contentType(MediaType.APPLICATION_JSON)
            .body(BodyInserters.fromPublisher(articleService.findByTag(tag), Article::class.java))
    }

    private fun getId(request: ServerRequest) = request.pathVariable("id")

    companion object {
        const val ARTICLE_ENDPOINT = "/article"
    }
}
