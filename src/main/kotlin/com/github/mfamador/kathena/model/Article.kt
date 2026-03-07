package com.github.mfamador.kathena.model

import org.springframework.data.annotation.Id
import org.springframework.data.elasticsearch.annotations.Document
import org.springframework.data.elasticsearch.annotations.Field
import org.springframework.data.elasticsearch.annotations.FieldType
import java.time.LocalDateTime

@Document(indexName = "blog")
data class Article(
    @Id var id: String? = null,
    @Field(type = FieldType.Text) var title: String? = null,
    @Field(type = FieldType.Text) var content: String? = null,
    @Field(type = FieldType.Keyword) var author: String? = null,
    @Field(type = FieldType.Keyword) var tags: List<String> = emptyList(),
    @Field(type = FieldType.Date) var publishedAt: LocalDateTime = LocalDateTime.now()
)
