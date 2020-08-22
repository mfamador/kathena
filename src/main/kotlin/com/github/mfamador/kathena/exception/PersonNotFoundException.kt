package com.github.mfamador.kathena.exception

class PersonNotFoundException(val personId: String) : RuntimeException("Person not found. Person Id: $personId")
