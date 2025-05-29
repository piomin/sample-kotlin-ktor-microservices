package pl.piomin.services

import com.fasterxml.jackson.databind.ObjectMapper
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.testing.*
import pl.piomin.services.model.Account
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class AccountAppTests {

    private val mapper: ObjectMapper = ObjectMapper()

    @Test
    fun testAdd() = testApplication {
        val response = client.post("/accounts") {
            contentType(ContentType.Application.Json)
            setBody(mapper.writeValueAsString(Account(1, 1000, "123456", 1)))
        }

        assertEquals(HttpStatusCode.OK, response.status)
        assertNotNull("Empty response", response.bodyAsText())
    }

    @Test
    fun testFindAll() = testApplication {
        val response = client.get("/accounts")
        assertEquals(HttpStatusCode.OK, response.status)
        assertNotNull("Empty response", response.bodyAsText())
    }

    @Test
    fun testFindById() = testApplication {
        val response = client.get("/accounts/1")
        assertEquals(HttpStatusCode.OK, response.status)
        assertNotNull("Empty response", response.bodyAsText())
        val a = mapper.readValue(response.bodyAsText(), Account::class.java)
        assertNotNull(a.id)
        assertEquals(1, a.id)
    }

}
