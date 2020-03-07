package com.github.mfamador.kathena.controller

import com.github.mfamador.kathena.messaging.EventProducer
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api")
class MessageApi(private val eventProducer: EventProducer) {

    @PostMapping("/message")
    fun publish(@RequestBody message: String) {
        eventProducer.send(message)
    }
}
