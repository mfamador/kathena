package com.github.mfamador.kathena

import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class KathenaApp
private val log = LoggerFactory.getLogger(KathenaApp::class.java)
fun main() {
    log.info("Starting Kathena")
    runApplication<KathenaApp>()
}
