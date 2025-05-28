package pl.piomin.services

import com.fasterxml.jackson.databind.ObjectMapper
import io.ktor.server.application.*
import io.ktor.http.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.server.testing.*
import pl.piomin.services.model.Customer
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class CustomerAppTests {

    private val mapper: ObjectMapper = ObjectMapper()

    @Test
    fun testAdd() = testApplication {
        application {
            main()
        }
        val response = client.post("/customers") {
            header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            setBody(mapper.writeValueAsString(Customer(1, "Test1")))
        }
        assertEquals(HttpStatusCode.OK, response.status)
        assertNotNull("Empty response", response.bodyAsText())
    }

    @Test
    fun testFindAll() = testApplication {
        application {
            main()
        }
        val response = client.get("/customers")
        assertEquals(HttpStatusCode.OK, response.status)
        assertNotNull("Empty response", response.bodyAsText())
    }

    @Test
    fun testFindById() = testApplication {
        application {
            main()
        }
        val response = client.get("/customers/1")
        assertEquals(HttpStatusCode.OK, response.status)
        assertNotNull("Empty response", response.bodyAsText())
        val a = mapper.readValue(response.bodyAsText(), Customer::class.java)
        assertNotNull(a.id)
        assertEquals(1, a.id)
    }

}