package pl.piomin.services

import com.fasterxml.jackson.databind.ObjectMapper
import io.ktor.server.application.*
import io.ktor.http.*
import io.ktor.server.testing.*
import pl.piomin.services.model.Customer
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class CustomerAppTests {

    private val mapper: ObjectMapper = ObjectMapper()

    @Test
    fun testAdd(): Unit = withTestApplication(Application::main) {
        with(handleRequest(HttpMethod.Post, "/customers"){
            addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            setBody(mapper.writeValueAsString(Customer(1, "Test1")))
        }) {
            assertEquals(io.ktor.http.HttpStatusCode.OK, response.status())
            assertNotNull("Empty response", response.content)
        }
    }

    @Test
    fun testFindAll() {
        withTestApplication(Application::main) {
            handleRequest(HttpMethod.Get, "/customers").apply {
                assertEquals(HttpStatusCode.OK, response.status())
                assertNotNull("Empty response", response.content)
            }
        }
    }

//    @Test
    fun testFindById() {
        withTestApplication(Application::main) {
            handleRequest(HttpMethod.Get, "/customers/1").apply {
                assertEquals(HttpStatusCode.OK, response.status())
                assertNotNull("Empty response", response.content)
                val a = mapper.readValue(response.content, Customer::class.java)
                assertNotNull(a.id)
                assertEquals(1, a.id)
            }
        }
    }

}
