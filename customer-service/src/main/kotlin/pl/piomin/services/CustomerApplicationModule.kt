package pl.piomin.services

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.apache.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.serialization.jackson.*
import io.ktor.server.application.*
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
import pl.piomin.services.feature.ConsulFeature
import pl.piomin.services.model.Customer
import pl.piomin.services.repository.CustomerRepository

fun Application.main() {

    val client = HttpClient(Apache) {
        install(ConsulFeature) {
            consulUrl = "http://localhost:8500"
        }
        install(io.ktor.client.plugins.contentnegotiation.ContentNegotiation) {
            jackson()
        }
    }

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
        get("/customers") {
            call.respond(message = CustomerRepository.getCustomers())
        }
        get("/customers/{id}") {
            val id: String? = call.parameters["id"]
            if (id != null) {
                val response: HttpResponse = client.get("http://account-service/accounts/customer/$id")
                val customer = CustomerRepository.getCustomers().first { it.id == id.toInt() }
                val customerRet = customer.copy(id = customer.id, name = customer.name)
                customerRet.accounts.addAll(response.body())
                call.respond(message = customerRet)
            }
        }
        post("/customers") {
            val customer: Customer = call.receive()
            customer.id = CustomerRepository.getCustomers().size + 1
            CustomerRepository.addCustomer(customer)
//            log.info("$customer")
            call.respond(message = customer)
        }
    }
}