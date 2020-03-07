package com.github.mfamador.kathena.messaging

import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component

@Component
class EventConsumer {
    private val log = LoggerFactory.getLogger(javaClass)

    @KafkaListener(topics = ["kathena-events"], groupId = "kathena-consumer")
    fun processMessage(message: String) {
        log.info("got message: {}", message)
    }
}
