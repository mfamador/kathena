package com.github.mfamador.kathena

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch

import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class KathenaApp

private val log = LoggerFactory.getLogger(KathenaApp::class.java)
suspend fun main() {
    log.info("Starting Kathena")

    val channel = Channel<Int>()
    GlobalScope.launch {
        for (x in 1..5) channel.send(x * x)
    }
    // here we print five received integers:
    repeat(5) { i ->
        println(channel.receive() + i)
    }
    println("Done!")

    runApplication<KathenaApp>()
}
