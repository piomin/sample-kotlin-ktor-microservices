package pl.piomin.services

import com.codahale.metrics.Slf4jReporter
import com.orbitz.consul.Consul
import com.orbitz.consul.model.agent.ImmutableRegistration
import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.application.log
import io.ktor.features.*
import io.ktor.http.HttpStatusCode
import io.ktor.jackson.jackson
import io.ktor.metrics.Metrics
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import org.slf4j.event.Level
import pl.piomin.services.model.Account
import pl.piomin.services.repository.AccountRepository
import java.util.concurrent.TimeUnit

fun main(args: Array<String>) {
    val consulClient = Consul.builder().withUrl("http://192.168.99.100:8500").build()
    val service = ImmutableRegistration.builder()
            .id("account-1")
            .name("account-service")
            .address("localhost")
            .port(8090)
            .build()
    consulClient.agentClient().register(service)
    embeddedServer(Netty, port = 8090, module = Application::module).start(wait = true)
}

fun Application.module() {
    val repository = AccountRepository()

    install(ContentNegotiation) {
        jackson {
        }
    }
    install(Metrics) {
        Slf4jReporter.forRegistry(registry).outputTo(log).build().start(10, TimeUnit.SECONDS)
    }
    install(CallLogging) {
        level = Level.TRACE
        callIdMdc("X-Request-ID")
    }
    install(CallId) {
        generate(10)
    }
    routing {
        get("/accounts") {
            call.respond(message = repository.accounts)
        }
        get("/accounts/{id}") {
            val id: String? = call.parameters["id"]
            if (id != null)
                call.respond(message = repository.accounts.filter { it.id == id.toInt() })
        }
        get("/accounts/customer/{customerId}") {
            val customerId: String? = call.parameters["customerId"]
            if (customerId != null)
                call.respond(message = repository.accounts.filter { it.customerId == customerId.toInt() })
        }
        post("/accounts") {
            var account: Account = call.receive()
            account.id = repository.accounts.size + 1
            repository.addAccount(account)
            log.info("$account")
            call.respond(message = account)
        }
    }
}

