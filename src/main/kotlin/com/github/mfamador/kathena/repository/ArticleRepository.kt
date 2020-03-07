package com.github.mfamador.kathena.repository

import com.github.mfamador.kathena.model.Article
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository

interface ArticleRepository : ElasticsearchRepository<Article, String>
