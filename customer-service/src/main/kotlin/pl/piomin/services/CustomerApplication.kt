package pl.piomin.services

import com.orbitz.consul.Consul
import com.orbitz.consul.model.agent.ImmutableRegistration
import io.ktor.server.engine.CommandLineConfig
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import pl.piomin.services.CustomerApplicationModule.main
import pl.piomin.services.model.Account

typealias Accounts = List<Account>

fun main(args: Array<String>) {
    val config = CommandLineConfig(args)
    val server = embeddedServer(Netty,
        port = config.port ?: 8081,
        host = config.host ?: "0.0.0.0",
        module = Application::main
    )

    val consulClient = Consul.builder().withUrl("http://localhost:8500").build()
    val service = ImmutableRegistration.builder()
            .id("customer-${server.environment.connectors[0].port}")
            .name("customer-service")
            .address("localhost")
            .port(server.environment.connectors[0].port)
            .build()
    consulClient.agentClient().register(service)

    server.start(wait = true)
}