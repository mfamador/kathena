package com.github.mfamador.kathena.controller

import com.github.mfamador.kathena.model.Person
import com.github.mfamador.kathena.service.PersonService
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.util.UriComponentsBuilder
import reactor.core.publisher.Mono

@Component
class PersonHandler(private val personService: PersonService) {

    fun getAll(request: ServerRequest): Mono<ServerResponse> =
        ServerResponse.ok().contentType(MediaType.APPLICATION_JSON)
            .body(BodyInserters.fromPublisher(personService.getAll(), Person::class.java))

    fun count(request: ServerRequest): Mono<ServerResponse> =
        ServerResponse.ok().contentType(MediaType.APPLICATION_JSON)
            .body(BodyInserters.fromPublisher(personService.count(), Long::class.java))

    fun get(request: ServerRequest): Mono<ServerResponse> =
        personService.get(getId(request))
            .flatMap { ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).syncBody(it) }
            .switchIfEmpty(ServerResponse.notFound().build())

    fun add(request: ServerRequest): Mono<ServerResponse> =
        ServerResponse.created(UriComponentsBuilder.fromPath("person/").build().toUri())
            .contentType(MediaType.APPLICATION_JSON)
            .body(
                BodyInserters.fromPublisher(
                    request.bodyToMono(Person::class.java)
                        .flatMap { p -> personService.save(p) }, Person::class.java
                )
            )

    fun update(request: ServerRequest): Mono<ServerResponse> =
        personService.get(getId(request))
            .flatMap {
                ServerResponse.ok().contentType(MediaType.APPLICATION_JSON)
                    .body(
                        BodyInserters.fromPublisher(
                            request.bodyToMono(Person::class.java)
                                .flatMap { p -> personService.save(p) }, Person::class.java
                        )
                    )
            }
            .switchIfEmpty(ServerResponse.notFound().build())

    fun delete(request: ServerRequest): Mono<ServerResponse> =
        personService.get(getId(request))
            .flatMap { p -> ServerResponse.noContent().build(personService.delete(p)) }
            .switchIfEmpty(ServerResponse.notFound().build())

    fun deleteAll(request: ServerRequest): Mono<ServerResponse> =
        ServerResponse.noContent().build(personService.deleteAll())

    private fun getId(request: ServerRequest) = request.pathVariable("id")

}
