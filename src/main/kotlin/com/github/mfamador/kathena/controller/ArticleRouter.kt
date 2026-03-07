package com.github.mfamador.kathena.controller

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.server.router

// Disabled - replaced by ArticleController for better Swagger documentation
// @Configuration
class ArticleRouter(private val handler: ArticleHandler) {

    @Bean
    fun articleRoute() = router {
        accept(MediaType.APPLICATION_JSON).nest {
            ARTICLE_ENDPOINT.nest {
                GET("", handler::getAll)
                GET("/{id}", handler::get)
                GET("/count", handler::count)
                POST("", handler::add)
                POST("/batch", handler::addBatch)
                PUT("/{id}", handler::update)
                DELETE("/{id}", handler::delete)
                DELETE("", handler::deleteAll)
                GET("/search", handler::searchByTitle)
                GET("/author/{author}", handler::findByAuthor)
                GET("/tag/{tag}", handler::findByTag)
            }
        }
    }

    companion object {
        const val ARTICLE_ENDPOINT = "/article"
    }
}

