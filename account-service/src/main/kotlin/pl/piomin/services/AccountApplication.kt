package pl.piomin.services

import com.orbitz.consul.Consul
import com.orbitz.consul.model.agent.ImmutableRegistration
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.Netty

fun main(args: Array<String>) {
    val server = embeddedServer(
        factory = Netty,
        configure = {
            val cliConfig = CommandLineConfig(args)
            takeFrom(cliConfig.engineConfig)
            loadCommonConfiguration(cliConfig.rootConfig.environment.config)
        }
    )
    val consulClient = Consul.builder().withUrl("http://localhost:8500").build()
    val service = ImmutableRegistration.builder()
            .id("account-${server.environment.config.port}")
            .name("account-service")
            .address("localhost")
            .port(server.environment.config.port)
            .build()
    consulClient.agentClient().register(service)

    server.start(wait = true)
}

