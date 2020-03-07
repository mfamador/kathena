package com.github.mfamador.kathena.repository

import com.github.mfamador.kathena.model.Person
import org.springframework.data.repository.reactive.ReactiveCrudRepository

interface PersonRepository : ReactiveCrudRepository<Person, String>
