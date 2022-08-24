package urlshortener

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.core.Is.`is`
import org.hamcrest.core.IsNull.notNullValue
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.http.HttpStatus
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.util.UriComponentsBuilder
import org.testcontainers.containers.GenericContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import java.net.URI


@Testcontainers
@RunWith(SpringRunner::class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class IntegrationTest {
    @Autowired
    private lateinit var restTemplate: TestRestTemplate

    @LocalServerPort
    private var port: Int = 0

    companion object {
        private const val FTP_EXAMPLE_COM = "ftp://example.com/"
        private const val HTTP_EXAMPLE_COM = "https://example.com/"
        private const val HASH_HTTP_EXAMPLE_COM = "83f94a17"
        private const val OTHER_VALUE = "f684a3c5"
        private const val REDIS_DEFAULT_PORT = 6379
        val LOCATION = UriComponentsBuilder.fromUriString("http://localhost:{port}/api/{hash}").build()

        @Container
        private val redisContainer = GenericContainer<Nothing>("redis:alpine").apply {
            withExposedPorts(REDIS_DEFAULT_PORT)
        }

        @JvmStatic
        @DynamicPropertySource
        fun properties(registry: DynamicPropertyRegistry) {
            redisContainer.start()
            registry.add("spring.redis.host") { redisContainer.host }
            registry.add("spring.redis.port") {
                println(redisContainer.getMappedPort(REDIS_DEFAULT_PORT))
                redisContainer.getMappedPort(REDIS_DEFAULT_PORT)
            }
        }
    }

    @Test
    fun `should return 201 and a location in a valid redirection`() {
        val parts = LinkedMultiValueMap<Any, Any>()
        parts.add("url", HTTP_EXAMPLE_COM)
        val response = restTemplate.postForEntity("/api", parts, String::class.java)
        assertThat(response.statusCode, `is`(HttpStatus.CREATED))
        val components = hashMapOf(
            "port" to port,
            "hash" to HASH_HTTP_EXAMPLE_COM
        )
        assertThat(response.headers.location, `is`(LOCATION.expand(components).toUri()))
    }

    @Test
    fun `should return 400 if the redirection cannot be created`() {
        val parts = LinkedMultiValueMap<Any, Any>()
        parts.add("url", FTP_EXAMPLE_COM)
        val response = restTemplate.postForEntity("/api", parts, String::class.java)
        assertThat(response.statusCode, `is`(HttpStatus.BAD_REQUEST))
    }

    @Test
    fun `should return 307 in a valid redirection`() {
        val parts = LinkedMultiValueMap<Any, Any>()
        parts.add("url", HTTP_EXAMPLE_COM)
        val created = restTemplate.postForEntity("/api", parts, String::class.java)
        assertThat(created.headers.location, `is`(notNullValue()))
        val path = created.headers.location!!.path
        val response = restTemplate.getForEntity(path, String::class.java)
        assertThat(response.statusCode, `is`(HttpStatus.TEMPORARY_REDIRECT))
        assertThat(response.headers.location, `is`(URI(HTTP_EXAMPLE_COM)))
    }

    @Test
    fun `should return 404 in an invalid redirection`() {
        val components = hashMapOf(
            "port" to port,
            "hash" to OTHER_VALUE
        )
        val path = LOCATION.expand(components).toUri()
        val response = restTemplate.getForEntity(path, String::class.java)
        assertThat(response.statusCode, `is`(HttpStatus.NOT_FOUND))
    }
}
