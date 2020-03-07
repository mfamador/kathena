package com.github.mfamador.kathena.messaging

import org.slf4j.LoggerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component

@Component
class EventProducer(private val kafkaTemplate: KafkaTemplate<String, String>) {
    private val log = LoggerFactory.getLogger(javaClass)

    fun send(message: String) {
        log.debug("sent message: {}", message)
        kafkaTemplate.send("kathena-events", message)
    }
}
