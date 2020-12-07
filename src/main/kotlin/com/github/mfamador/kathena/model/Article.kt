package com.github.mfamador.kathena.model

import org.springframework.data.annotation.Id
import org.springframework.data.elasticsearch.annotations.Document

@Document(indexName = "blog")
data class Article(@Id var id: String?, var title: String?)
