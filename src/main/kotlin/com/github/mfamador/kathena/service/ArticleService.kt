package com.github.mfamador.kathena.service

import com.github.mfamador.kathena.model.Article
import com.github.mfamador.kathena.repository.ArticleRepository
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers

@Service
class ArticleService(private val articleRepository: ArticleRepository) {
    fun get(id: String): Mono<Article> = Mono.fromCallable {
        articleRepository.findById(id).orElse(null)
    }
        .subscribeOn(Schedulers.boundedElastic())
        .flatMap { article ->
            if (article == null) Mono.empty() else Mono.just(article)
        }

    fun getAll(): Flux<Article> = Flux.fromIterable(articleRepository.findAll())
        .subscribeOn(Schedulers.boundedElastic())

    fun count(): Mono<Long> = Mono.fromCallable { articleRepository.count() }
        .subscribeOn(Schedulers.boundedElastic())

    fun save(article: Article): Mono<Article> = Mono.fromCallable { articleRepository.save(article) }
        .subscribeOn(Schedulers.boundedElastic())

    fun saveAll(articles: List<Article>): Flux<Article> = Flux.fromIterable(articles)
        .flatMap { save(it) }

    fun delete(id: String): Mono<Void> = Mono.fromCallable {
        articleRepository.deleteById(id)
        null
    }
        .subscribeOn(Schedulers.boundedElastic())
        .then()

    fun deleteAll(): Mono<Void> = Mono.fromCallable {
        articleRepository.deleteAll()
        null
    }
        .subscribeOn(Schedulers.boundedElastic())
        .then()
    
    // Search operations
    fun searchByTitle(title: String): Flux<Article> = Flux.fromIterable(articleRepository.findByTitleContaining(title))
        .subscribeOn(Schedulers.boundedElastic())
    
    fun findByAuthor(author: String): Flux<Article> = Flux.fromIterable(articleRepository.findByAuthor(author))
        .subscribeOn(Schedulers.boundedElastic())
    
    fun findByTag(tag: String): Flux<Article> = Flux.fromIterable(articleRepository.findByTagsContaining(tag))
        .subscribeOn(Schedulers.boundedElastic())
}
