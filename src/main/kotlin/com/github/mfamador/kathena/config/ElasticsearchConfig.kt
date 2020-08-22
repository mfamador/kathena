package com.github.mfamador.kathena.config

import org.elasticsearch.client.RestHighLevelClient
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.elasticsearch.client.ClientConfiguration
import org.springframework.data.elasticsearch.client.RestClients

@Configuration
class ElasticsearchConfig(@Value("\${elasticsearch.host}") private val host: String) {

    @Bean
    fun client(): RestHighLevelClient {
        val clientConfiguration = ClientConfiguration.builder()
                .connectedTo(host)
                .build()
        return RestClients.create(clientConfiguration).rest()
    }
}
