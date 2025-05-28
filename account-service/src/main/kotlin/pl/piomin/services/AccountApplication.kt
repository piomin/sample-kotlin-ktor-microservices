package pl.piomin.services

import com.orbitz.consul.Consul
import com.orbitz.consul.model.agent.ImmutableRegistration
import io.ktor.server.engine.CommandLineConfig
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty

fun main(args: Array<String>) {
    val config = CommandLineConfig(args)
    val server = embeddedServer(Netty,
        port = config.port ?: 8080,
        host = config.host ?: "0.0.0.0",
        module = Application::main
    )
    val consulClient = Consul.builder().withUrl("http://localhost:8500").build()
    val service = ImmutableRegistration.builder()
            .id("account-${server.environment.connectors[0].port}")
            .name("account-service")
            .address("localhost")
            .port(server.environment.connectors[0].port)
            .build()
    consulClient.agentClient().register(service)

    server.start(wait = true)
}

