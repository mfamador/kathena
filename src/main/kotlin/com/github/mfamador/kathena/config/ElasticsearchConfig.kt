package com.github.mfamador.kathena.config

import org.apache.http.HttpHost
import org.elasticsearch.client.RestClient
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class ElasticsearchConfig(@Value("\${elasticsearch.host}") private val host: String) {

    @Bean
    fun client(): RestClient {
        return RestClient.builder(HttpHost.create(host))
            .build()
    }
}
