package com.github.mfamador.kathena.model

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document
data class Person(@Id var id: String?, var name :String?)
