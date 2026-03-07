package com.github.mfamador.kathena.repository

import com.github.mfamador.kathena.model.Person
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import reactor.core.publisher.Flux

interface PersonRepository : ReactiveCrudRepository<Person, String> {
    fun findByNameContainingIgnoreCase(name: String): Flux<Person>
    fun findByCity(city: String): Flux<Person>
    fun findByAgeGreaterThan(age: Int): Flux<Person>
}
