package com.github.mfamador.kathena.config

import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.info.Contact
import io.swagger.v3.oas.models.servers.Server
import org.springdoc.core.models.GroupedOpenApi
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class OpenApiConfig {

    @Bean
    fun customOpenAPI(): OpenAPI {
        return OpenAPI()
            .info(
                Info()
                    .title("Kathena API")
                    .version("1.0")
                    .description("""
                        Spring Boot + WebFlux playground with Reactive MongoDB, Elasticsearch, and Kafka.
                        
                        This API demonstrates:
                        - Reactive programming with Spring WebFlux
                        - MongoDB for document storage (Person entities)
                        - Elasticsearch for full-text search (Article entities)
                        - Kafka for event streaming
                    """.trimIndent())
                    .contact(
                        Contact()
                            .name("Kathena")
                            .url("https://github.com/mfamador/kathena")
                    )
            )
            .servers(
                listOf(
                    Server()
                        .url("http://localhost:8080")
                        .description("Local development server")
                )
            )
    }

    @Bean
    fun personApi(): GroupedOpenApi {
        return GroupedOpenApi.builder()
            .group("person")
            .displayName("Person API (MongoDB)")
            .pathsToMatch("/person/**")
            .build()
    }

    @Bean
    fun articleApi(): GroupedOpenApi {
        return GroupedOpenApi.builder()
            .group("article")
            .displayName("Article API (Elasticsearch)")
            .pathsToMatch("/article/**")
            .build()
    }

    @Bean
    fun messagingApi(): GroupedOpenApi {
        return GroupedOpenApi.builder()
            .group("messaging")
            .displayName("Messaging API (Kafka)")
            .pathsToMatch("/api/message/**")
            .build()
    }

    @Bean
    fun dataManagementApi(): GroupedOpenApi {
        return GroupedOpenApi.builder()
            .group("data-management")
            .displayName("Data Management API")
            .pathsToMatch("/api/data/**")
            .build()
    }

    @Bean
    fun allApis(): GroupedOpenApi {
        return GroupedOpenApi.builder()
            .group("all")
            .displayName("All APIs")
            .pathsToMatch("/**")
            .build()
    }
}

