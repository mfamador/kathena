package com.github.mfamador.kathena.controller

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.server.router

@Configuration
class PersonRouter(private val handler: PersonHandler) {

    @Bean
    fun route() = router {
        accept(MediaType.APPLICATION_JSON).nest {
            PERSON_ENDPOINT.nest {
                GET("", handler::getAll)
                GET("/{id}", handler::get)
                GET("/count", handler::count)
                POST("", handler::add)
                PUT("/{id}", handler::update)
                GET("/{id}", handler::get)
                DELETE("/{id}", handler::delete)
                DELETE("", handler::deleteAll) // TODO - remove this route
            }
        }
    }

    companion object {
        const val PERSON_ENDPOINT = "/person"
    }
}
