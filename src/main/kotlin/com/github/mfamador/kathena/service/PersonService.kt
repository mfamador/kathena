package com.github.mfamador.kathena.service

import com.github.mfamador.kathena.model.Person
import com.github.mfamador.kathena.repository.PersonRepository
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Service
class PersonService(private val personRepository: PersonRepository) {
    fun get(id: String): Mono<Person> = personRepository.findById(id)
    fun getAll(): Flux<Person> = personRepository.findAll()
    fun count(): Mono<Long> = personRepository.count()
    fun save(person: Person): Mono<Person> = personRepository.save(person)
    fun saveAll(persons: List<Person>): Flux<Person> = personRepository.saveAll(persons)
    fun delete(person: Person): Mono<Void> = personRepository.delete(person)
    fun deleteAll(): Mono<Void> = personRepository.deleteAll()
    
    // Search operations
    fun searchByName(name: String): Flux<Person> = personRepository.findByNameContainingIgnoreCase(name)
    fun findByCity(city: String): Flux<Person> = personRepository.findByCity(city)
    fun findByAgeGreaterThan(age: Int): Flux<Person> = personRepository.findByAgeGreaterThan(age)
}
