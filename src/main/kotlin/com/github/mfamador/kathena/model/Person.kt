package com.github.mfamador.kathena.model

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDateTime

@Document
data class Person(
    @Id var id: String? = null,
    var name: String? = null,
    var email: String? = null,
    var age: Int? = null,
    var city: String? = null,
    var createdAt: LocalDateTime = LocalDateTime.now()
)
