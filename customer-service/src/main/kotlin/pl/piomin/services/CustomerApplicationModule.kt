package pl.piomin.services

import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.application.log
import io.ktor.client.HttpClient
import io.ktor.client.engine.apache.Apache
import io.ktor.client.request.get
import io.ktor.features.*
import io.ktor.jackson.jackson
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.routing
import org.slf4j.event.Level
import pl.piomin.services.feature.ConsulFeature
import pl.piomin.services.model.Customer
import pl.piomin.services.repository.CustomerRepository

fun Application.main() {

    val repository = CustomerRepository()
    val client = HttpClient(Apache) {
        install(ConsulFeature) {
            consulUrl = "http://192.168.99.100:8500"
        }
    }

    install(ContentNegotiation) {
        jackson {
        }
    }
//    install(Metrics) {
//        Slf4jReporter.forRegistry(registry).outputTo(log).build().start(10, TimeUnit.SECONDS)
//    }
    install(CallLogging) {
        level = Level.TRACE
        callIdMdc("X-Request-ID")
    }
    install(CallId) {
        generate(10)
    }
    routing {
        get("/customers") {
            call.respond(message = repository.customers)
        }
        get("/customers/{id}") {
            val id: String? = call.parameters["id"]
            if (id != null) {
                val accounts = client.get<Accounts>("http://account-service/accounts/customer/$id")
                val customer = repository.customers.filter { it.id == id.toInt() }.last()
                customer.accounts.addAll(accounts)
                call.respond(message = customer)
            }
        }
        post("/customers") {
            val customer: Customer = call.receive()
            customer.id = repository.customers.size + 1
            repository.addCustomer(customer)
            log.info("$customer")
            call.respond(message = customer)
        }
    }
}