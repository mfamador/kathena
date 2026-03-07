package com.github.mfamador.kathena.controller

import com.github.mfamador.kathena.model.Person
import com.github.mfamador.kathena.service.PersonService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/person")
@Tag(name = "Person", description = "Person management APIs (MongoDB - Reactive)")
class PersonController(private val personService: PersonService) {

    @GetMapping
    @Operation(
        summary = "Get all persons",
        description = "Retrieve all persons from MongoDB",
        responses = [ApiResponse(responseCode = "200", description = "Success")]
    )
    fun getAll(): Flux<Person> = personService.getAll()

    @GetMapping("/{id}")
    @Operation(
        summary = "Get person by ID",
        description = "Retrieve a specific person by their ID",
        responses = [
            ApiResponse(responseCode = "200", description = "Success"),
            ApiResponse(responseCode = "404", description = "Person not found")
        ]
    )
    fun get(
        @Parameter(description = "Person ID")
        @PathVariable id: String
    ): Mono<Person> = personService.get(id)

    @GetMapping("/count")
    @Operation(
        summary = "Count all persons",
        description = "Get the total number of persons in the database",
        responses = [ApiResponse(responseCode = "200", description = "Success")]
    )
    fun count(): Mono<Long> = personService.count()

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
        summary = "Create a new person",
        description = "Add a new person to MongoDB",
        responses = [ApiResponse(responseCode = "201", description = "Person created successfully")]
    )
    fun add(@RequestBody person: Person): Mono<Person> = personService.save(person)

    @PostMapping("/batch")
    @Operation(
        summary = "Create multiple persons",
        description = "Add multiple persons to MongoDB in a single batch operation",
        responses = [ApiResponse(responseCode = "200", description = "Persons created successfully")]
    )
    fun addBatch(@RequestBody persons: List<Person>): Flux<Person> = personService.saveAll(persons)

    @PutMapping("/{id}")
    @Operation(
        summary = "Update person",
        description = "Update an existing person by ID",
        responses = [
            ApiResponse(responseCode = "200", description = "Person updated successfully"),
            ApiResponse(responseCode = "404", description = "Person not found")
        ]
    )
    fun update(
        @Parameter(description = "Person ID")
        @PathVariable id: String,
        @RequestBody person: Person
    ): Mono<Person> = personService.save(person.apply { this.id = id })

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(
        summary = "Delete person",
        description = "Delete a person by ID",
        responses = [
            ApiResponse(responseCode = "204", description = "Person deleted successfully"),
            ApiResponse(responseCode = "404", description = "Person not found")
        ]
    )
    fun delete(
        @Parameter(description = "Person ID")
        @PathVariable id: String
    ): Mono<Void> = personService.get(id).flatMap { personService.delete(it) }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(
        summary = "Delete all persons",
        description = "Delete all persons from the database (use with caution)",
        responses = [ApiResponse(responseCode = "204", description = "All persons deleted")]
    )
    fun deleteAll(): Mono<Void> = personService.deleteAll()

    @GetMapping("/search")
    @Operation(
        summary = "Search persons by name",
        description = "Search for persons by name (case-insensitive partial match)",
        responses = [ApiResponse(responseCode = "200", description = "Success")]
    )
    fun searchByName(
        @Parameter(description = "Name to search for", example = "Alice")
        @RequestParam(required = false, defaultValue = "") name: String
    ): Flux<Person> = personService.searchByName(name)

    @GetMapping("/city/{city}")
    @Operation(
        summary = "Find persons by city",
        description = "Retrieve all persons living in a specific city",
        responses = [ApiResponse(responseCode = "200", description = "Success")]
    )
    fun findByCity(
        @Parameter(description = "City name", example = "New York")
        @PathVariable city: String
    ): Flux<Person> = personService.findByCity(city)

    @GetMapping("/older")
    @Operation(
        summary = "Find persons older than age",
        description = "Retrieve all persons older than the specified age",
        responses = [ApiResponse(responseCode = "200", description = "Success")]
    )
    fun findByAgeGreaterThan(
        @Parameter(description = "Minimum age", example = "30")
        @RequestParam(required = false, defaultValue = "0") age: Int
    ): Flux<Person> = personService.findByAgeGreaterThan(age)
}

