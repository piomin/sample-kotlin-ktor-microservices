package pl.piomin.services

import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.application.install
import io.ktor.server.application.log
import io.ktor.serialization.jackson.*
import io.ktor.server.metrics.micrometer.*
import io.ktor.server.plugins.callid.*
import io.ktor.server.plugins.calllogging.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
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
            call.respond(message = AccountRepository.getAccounts())
        }
        get("/accounts/{id}") {
            val id: String? = call.parameters["id"]
            if (id != null)
                call.respond(message = AccountRepository.getAccounts()
                    .first { it.id == id.toInt() })

        }
        get("/accounts/customer/{customerId}") {
            val customerId: String? = call.parameters["customerId"]
            if (customerId != null)
                call.respond(message = AccountRepository.getAccounts()
                    .filter { it.customerId == customerId.toInt() })
        }
        post("/accounts") {
            var account: Account = call.receive()
            account.id = AccountRepository.getAccounts().size + 1
            AccountRepository.addAccount(account)
//            log.info("$account")
            call.respond(message = account)
        }
    }
}