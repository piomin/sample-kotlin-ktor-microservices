package pl.piomin.services

import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.application.log
import io.ktor.client.HttpClient
import io.ktor.client.engine.apache.Apache
import io.ktor.client.features.json.JacksonSerializer
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.request.get
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
import pl.piomin.services.feature.ConsulFeature
import pl.piomin.services.model.Customer
import pl.piomin.services.repository.CustomerRepository

fun Application.main() {

    val client = HttpClient(Apache) {
        install(ConsulFeature) {
            consulUrl = "http://192.168.99.100:8500"
        }
        install(JsonFeature) {
            serializer = JacksonSerializer()
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
                val accounts = client.get<Accounts>("http://account-service/accounts/customer/$id")
                val customer = CustomerRepository.getCustomers().first { it.id == id.toInt() }
                val customerRet = customer.copy(id = customer.id, name = customer.name)
                customerRet.accounts.addAll(accounts)
                call.respond(message = customerRet)
            }
        }
        post("/customers") {
            val customer: Customer = call.receive()
            customer.id = CustomerRepository.getCustomers().size + 1
            CustomerRepository.addCustomer(customer)
            log.info("$customer")
            call.respond(message = customer)
        }
    }
}