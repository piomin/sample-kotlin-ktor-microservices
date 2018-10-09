package pl.piomin.services

import com.orbitz.consul.Consul
import com.orbitz.consul.model.agent.ImmutableRegistration
import io.ktor.server.engine.commandLineEnvironment
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty

fun main(args: Array<String>) {
    val server = embeddedServer(Netty, commandLineEnvironment(args))
    val consulClient = Consul.builder().withUrl("http://192.168.99.100:8500").build()
    val service = ImmutableRegistration.builder()
            .id("account-${server.environment.connectors[0].port}")
            .name("account-service")
            .address("localhost")
            .port(server.environment.connectors[0].port)
            .build()
    consulClient.agentClient().register(service)

    server.start(wait = true)
}

