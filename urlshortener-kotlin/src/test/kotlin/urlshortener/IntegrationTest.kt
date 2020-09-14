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
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.http.HttpStatus
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.util.UriComponentsBuilder
import java.net.URI
import java.util.*


@RunWith(SpringRunner::class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class IntegrationTest {
    @Autowired
    private lateinit var restTemplate: TestRestTemplate

    @LocalServerPort
    private var port: Int = 0

    companion object {
        const val HTTP_EXAMPLE_COM = "http://example.com/"
        const val HASH_HTTP_EXAMPLE_COM = "f684a3c4"
        val LOCATION = UriComponentsBuilder.fromUriString("http://localhost:{port}/api/{hash}").build()
    }

    @Test
    fun testCreation() {
        val parts = LinkedMultiValueMap<Any, Any>()
        parts.add("url", HTTP_EXAMPLE_COM)
        val response = restTemplate.postForEntity("/api", parts, String::class.java)
        assertThat(response.statusCode, `is`(HttpStatus.CREATED))
        val components: MutableMap<String, Any> = HashMap()
        components["port"] = port
        components["hash"] = HASH_HTTP_EXAMPLE_COM
        assertThat(response.headers.location, `is`(LOCATION.expand(components).toUri()))
    }

    @Test
    fun testRedirection() {
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
