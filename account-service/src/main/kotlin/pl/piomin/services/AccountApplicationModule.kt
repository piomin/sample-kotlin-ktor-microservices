package pl.piomin.services

import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.application.log
import io.ktor.features.*
import io.ktor.jackson.jackson
import io.ktor.metrics.micrometer.MicrometerMetrics
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.routing
import io.micrometer.core.instrument.binder.jvm.ClassLoaderMetrics
import io.micrometer.core.instrument.binder.jvm.JvmGcMetrics
import io.micrometer.core.instrument.binder.jvm.JvmMemoryMetrics
import io.micrometer.core.instrument.binder.jvm.JvmThreadMetrics
import io.micrometer.core.instrument.binder.system.FileDescriptorMetrics
import io.micrometer.core.instrument.binder.system.ProcessorMetrics
import io.micrometer.core.instrument.simple.SimpleMeterRegistry
import org.slf4j.event.Level
import pl.piomin.services.model.Account
import pl.piomin.services.repository.AccountRepository

fun Application.main() {
    val repository = AccountRepository()

    install(ContentNegotiation) {
        jackson {
        }
    }
    install(MicrometerMetrics) {
        registry = SimpleMeterRegistry()
        meterBinders = listOf(
                ClassLoaderMetrics(),
                JvmMemoryMetrics(),
                JvmGcMetrics(),
                ProcessorMetrics(),
                JvmThreadMetrics(),
                FileDescriptorMetrics()
        )
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