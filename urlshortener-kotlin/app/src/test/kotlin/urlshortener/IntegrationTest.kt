package urlshortener

import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.MatcherAssert.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.http.HttpStatus
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.util.UriComponents
import org.springframework.web.util.UriComponentsBuilder
import org.testcontainers.containers.GenericContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import java.net.URI

@Testcontainers
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class IntegrationTest {
    @Autowired
    private lateinit var restTemplate: TestRestTemplate

    @LocalServerPort
    private var port: Int = 0

    companion object {
        private const val HTTP_EXAMPLE_COM = "https://example.com/"
        private const val HASH_HTTP_EXAMPLE_COM = "83f94a17"
        private const val REDIS_DEFAULT_PORT = 6379
        val LOCATION: UriComponents = UriComponentsBuilder.fromUriString("http://localhost:{port}/api/{hash}").build()

        @Container
        private val redisContainer = GenericContainer<Nothing>("redis:alpine").apply {
            withExposedPorts(REDIS_DEFAULT_PORT)
        }

        @JvmStatic
        @DynamicPropertySource
        fun properties(registry: DynamicPropertyRegistry) {
            redisContainer.start()
            registry.add("spring.redis.host") { redisContainer.host }
            registry.add("spring.redis.port") { redisContainer.getMappedPort(REDIS_DEFAULT_PORT) }
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

}
