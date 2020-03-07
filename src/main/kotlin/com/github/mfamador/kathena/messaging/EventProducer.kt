package com.github.mfamador.kathena.messaging

import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component

@Component
class EventProducer(private val kafkaTemplate: KafkaTemplate<String, String>) {

    fun send(message: String) {
        kafkaTemplate.send("kathena-events", message)
    }
}
