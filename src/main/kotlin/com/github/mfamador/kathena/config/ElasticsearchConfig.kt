package com.github.mfamador.kathena.config

import org.elasticsearch.client.RestHighLevelClient
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.elasticsearch.client.ClientConfiguration
import org.springframework.data.elasticsearch.client.RestClients


@Configuration
class ElasticsearchConfig {
    val log = LoggerFactory.getLogger(javaClass)

    @Bean
    fun client(): RestHighLevelClient {

        val clientConfiguration = ClientConfiguration.builder()
            .connectedTo("localhost:9200", "localhost:9201")
            .build()

        return RestClients.create(clientConfiguration).rest()
    }

}
