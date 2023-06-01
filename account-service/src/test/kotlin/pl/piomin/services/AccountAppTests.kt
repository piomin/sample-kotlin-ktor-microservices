package pl.piomin.services

import com.fasterxml.jackson.databind.ObjectMapper
import io.ktor.server.application.*
import io.ktor.http.*
import io.ktor.server.testing.*
import pl.piomin.services.model.Account
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class AccountAppTests {

    private val mapper: ObjectMapper = ObjectMapper()

    @Test
    fun testAdd(): Unit = withTestApplication(Application::main) {
        with(handleRequest(HttpMethod.Post, "/accounts"){
            addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            setBody(mapper.writeValueAsString(Account(1, 1000, "123456", 1)))
        }) {
            assertEquals(HttpStatusCode.OK, response.status())
            assertNotNull("Empty response", response.content)
        }
    }

    @Test
    fun testFindAll() {
        withTestApplication(Application::main) {
            handleRequest(HttpMethod.Get, "/accounts").apply {
                assertEquals(HttpStatusCode.OK, response.status())
                assertNotNull("Empty response", response.content)
            }
        }
    }

    @Test
    fun testFindById() {
        withTestApplication(Application::main) {
            handleRequest(HttpMethod.Get, "/accounts/1").apply {
                assertEquals(HttpStatusCode.OK, response.status())
                assertNotNull("Empty response", response.content)
                val a = mapper.readValue(response.content, Account::class.java)
                assertNotNull(a.id)
                assertEquals(1, a.id)
            }
        }
    }
}
