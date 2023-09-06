# Create a URL Shortener step-by-step

The goal of this tutorial is the creation of a powerful URL shortener with a few lines of Kotlin.

## What is a URL shortener

A URL shortener is a web service that makes a long URL short, easy to remember and to share. The behaviour of the
application is as follows:

![URL shortener flow](https://github.com/UNIZAR-30246-WebEngineering/tutorials/blob/master/urlshortener-kotlin/img/flow.png)

Remember, the focus of this course is Web Engineering, therefore we will focus on the red interactions in the above
figure.

## Prerequisites

Prerequisites:

- [Java 17](http://www.java.com/en/) or higher.
- [Gradle Build Tool 8.0](http://www.gradle.org/) or higher.
- [Redis 3.0](http://redis.io/download) or higher.
- [HTTPie](https://httpie.org/) or similar HTTP client for testing.
- [Visual Studio Code](https://code.visualstudio.com/) or similar as editor.
- [Docker](https://www.docker.com/).

## Create the project stub

Run:

```bash
$ mkdir urlshortener
$ cd urlshortener
$ gradle init

Select type of project to generate:
  1: basic
  2: application
  3: library
  4: Gradle plugin
Enter selection (default: basic) [1..4] 2

Select implementation language:
  1: C++
  2: Groovy
  3: Java
  4: Kotlin
  5: Scala
  6: Swift
Enter selection (default: Java) [1..5] 4

Generate multiple subprojects for application? (default: no) [yes, no] no

Generate multiple subprojects for application? (default: no) [yes, no] no
Select build script DSL:
  1: Kotlin
  2: Groovy
Enter selection (default: Kotlin) [1..2] 1

Project name (default: urlshortener): urlshortener
Source package (default: urlshortener): urlshortener
Enter target version of Java (min. 7) (default: 20): 17
```

And then run:

```bash
gradle run
```

## Transform into a Spring Boot Web application

Update `app/build.gradle.kts` so it looks like:

```kotlin
plugins {
    kotlin("jvm") version "1.9.10"
    kotlin("plugin.spring") version "1.9.10"
    id("org.springframework.boot") version "3.1.3"
}
```

in the `dependencies` block add:

```kotlin
implementation(platform(org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES))
implementation("org.springframework.boot:spring-boot-starter-web")
```

and remove the `application` block

```kotlin
application {
    // Define the main class for the application.
    mainClass.set("urlshortener.AppKt")
}
```

Edit the class `App` at `app/src/main/kotlin/urlshortener/App.kt` and rewrite the code as follows:

```kotlin
package urlshortener

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class App

fun main(args: Array<String>) {
    runApplication<App>(args = args)
}
```

Remove the file  `app/src/test/kotlin/urlshortener/AppTest.kt`.

```bash
rm app/src/test/kotlin/urlshortener/AppTest.kt
```

And then run:

```bash
gradle bootRun
```

You have now a do-nothing web server listening at port 8080. Let's test it by running in a different terminal:

```bash
http -v localhost:8080
```

This is the client request (HTTPie):

```http
GET / HTTP/1.1
Accept: */*
Accept-Encoding: gzip, deflate
Connection: keep-alive
Host: localhost:8080
User-Agent: HTTPie/3.2.1

```

And this the server response (our `App`):

```http
HTTP/1.1 404 
Connection: keep-alive
Content-Type: application/json
Date: Wed, 06 Sep 2023 17:49:31 GMT
Keep-Alive: timeout=60
Transfer-Encoding: chunked
Vary: Origin
Vary: Access-Control-Request-Method
Vary: Access-Control-Request-Headers

{
    "error": "Not Found",
    "message": "",
    "path": "/",
    "status": 404,
    "timestamp": "2023-09-06T17:49:31.657+00:00"
}
```

This server can be killed with ```Ctrl-C```.

## URL Shortener version 0

Edit the class `App` and rewrite the code as follows:

```Kotlin
package urlshortener

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.servlet.HandlerMapping
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse

@SpringBootApplication
class App

fun main(args: Array<String>) {
    runApplication<App>(args = args)
}

@Controller
class RedirectController {
    @GetMapping("/**")
    fun redirectTo(req: HttpServletRequest, resp: HttpServletResponse) {
        resp.sendRedirect(
            req.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE)
                .toString().substring(1)
        )
    }
}
```

You have now a redirecting endpoint at port 8080. Let's test it:

```bash
http -v localhost:8080/http://www.unizar.es/
```

This is the HTTP request.

```http
GET /http://www.unizar.es/ HTTP/1.1
Accept: */*
Accept-Encoding: gzip, deflate
Connection: keep-alive
Host: localhost:8080
User-Agent: HTTPie/3.2.1

```

This is the HTTP response:

```http
HTTP/1.1 302 
Connection: keep-alive
Content-Length: 0
Date: Wed, 06 Sep 2023 17:50:50 GMT
Keep-Alive: timeout=60
Location: http:/www.unizar.es/

```

## URL Shortener version 1

Edit the class `App` and rewrite the code as follows:

```kotlin
package urlshortener

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.util.MultiValueMap
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import java.net.URI
import java.nio.charset.StandardCharsets
import jakarta.servlet.http.HttpServletRequest


@SpringBootApplication
class App

fun main(args: Array<String>) {
    runApplication<App>(args = args)
}

@Controller
class RedirectController {

    private val sharedData = mutableMapOf<String, String>()

    @GetMapping("/{id}")
    fun redirectTo(@PathVariable id: String) =
        sharedData[id]?.let {
            ResponseEntity<Unit>(HttpHeaders().apply {
                location = URI.create(it)
            }, HttpStatus.TEMPORARY_REDIRECT)
        } ?: ResponseEntity(HttpStatus.NOT_FOUND)

    @PostMapping
    fun shortener(@RequestParam form: MultiValueMap<String?, String?>, req: HttpServletRequest): ResponseEntity<Unit> {
        val url = form.getFirst("url") ?: return ResponseEntity(HttpStatus.BAD_REQUEST)
        val id = url.hashCode().toString()
        sharedData[id] = url
        return ResponseEntity(HttpHeaders().apply { 
          location = URI.create(req.requestURL.append(id).toString()) 
         }, HttpStatus.CREATED)
    }
}
```

Run it now, and you will have a working shortener endpoint at port 8080.
Let's test it:

```bash
http -v --form POST localhost:8080 url=http://www.unizar.es/
```

This is the HTTP request.

```http
POST / HTTP/1.1
Accept: */*
Accept-Encoding: gzip, deflate
Connection: keep-alive
Content-Length: 33
Content-Type: application/x-www-form-urlencoded; charset=utf-8
Host: localhost:8080
User-Agent: HTTPie/3.2.1

url=http%3A%2F%2Fwww.unizar.es%2F
```

This is the HTTP response.

```http
HTTP/1.1 201 
Connection: keep-alive
Content-Length: 0
Date: Wed, 06 Sep 2023 17:52:29 GMT
Keep-Alive: timeout=60
Location: http://localhost:8080/2108188503

```

Let's test the returned `Location`.

```bash
http -v localhost:8080/2108188503
```

This is the HTTP request.

```http
GET /2108188503 HTTP/1.1
Accept: */*
Accept-Encoding: gzip, deflate
Connection: keep-alive
Host: localhost:8080
User-Agent: HTTPie/3.2.1

```

This is the HTTP response.

```http
HTTP/1.1 307 
Connection: keep-alive
Content-Length: 0
Date: Wed, 06 Sep 2023 17:52:50 GMT
Keep-Alive: timeout=60
Location: http://www.unizar.es/

```

A request with a different value will raise an error.

```bash
http -v localhost:8080/3108188502
```

This is the HTTP request.

```http
GET /3108188502 HTTP/1.1
Accept: */*
Accept-Encoding: gzip, deflate
Connection: keep-alive
Host: localhost:8080
User-Agent: HTTPie/3.2.1

```

This is the HTTP response.

```http
HTTP/1.1 404 
Connection: keep-alive
Content-Length: 0
Date: Wed, 06 Sep 2023 17:53:12 GMT
Keep-Alive: timeout=60

```

## URL Shortener secured (version 2)

Add the following dependencies to `build.gradle.kts` in the `dependencies` block:

```kotlin
    implementation("commons-validator:commons-validator:1.7")
    implementation("com.google.guava:guava:32.1.1-jre") // Only if it is not already present
```

Note that the version is not managed in these libraries.

This happens because `guava` and `commons-validator` are not used in the Spring Framework projects.

Edit the class `App` and rewrite the code as follows:

```kotlin
package urlshortener

import com.google.common.hash.Hashing
import org.apache.commons.validator.routines.UrlValidator
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.util.MultiValueMap
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import java.net.URI
import java.nio.charset.StandardCharsets
import jakarta.servlet.http.HttpServletRequest


@SpringBootApplication
class App

fun main(args: Array<String>) {
    runApplication<App>(*args)
}

@Controller
class RedirectController {

    private val sharedData = mutableMapOf<String, String>()

    @GetMapping("/{id}")
    fun redirectTo(@PathVariable id: String) =
        sharedData[id]?.let {
            ResponseEntity<Unit>(HttpHeaders().apply {
                location = URI.create(it)
            }, HttpStatus.TEMPORARY_REDIRECT)
        } ?: ResponseEntity(HttpStatus.NOT_FOUND)

    @PostMapping
    fun shortener(@RequestParam form: MultiValueMap<String?, String?>, req: HttpServletRequest): ResponseEntity<Unit> {
        val url = form.getFirst("url")
        val urlValidator = UrlValidator(arrayOf("http", "https"))
        return when {
            url == null -> ResponseEntity(HttpStatus.BAD_REQUEST)
            urlValidator.isValid(url) -> {
                val id = Hashing.murmur3_32_fixed().hashString(url, StandardCharsets.UTF_8).toString()
                sharedData[id] = url
                ResponseEntity(HttpHeaders().apply {
                    location = URI.create(req.requestURL.append(id).toString())
                }, HttpStatus.CREATED)
            }
            else -> ResponseEntity(HttpStatus.BAD_REQUEST)
        }
    }
}
```

Run `gradle bootRun`, then run again the server and test a bad request:

```bash
http -v --form POST localhost:8080 url=ftp://www.unizar.es/
```

This is the HTTP request.

```http
POST / HTTP/1.1
Accept: */*
Accept-Encoding: gzip, deflate
Connection: keep-alive
Content-Length: 32
Content-Type: application/x-www-form-urlencoded; charset=utf-8
Host: localhost:8080
User-Agent: HTTPie/3.2.1

url=ftp%3A%2F%2Fwww.unizar.es%2F

```

This is the HTTP response.

```http
HTTP/1.1 400 
Connection: close
Content-Length: 0
Date: Wed, 06 Sep 2023 17:57:34 GMT

```

## Scalable URL Shortener (version 3)

Add the following dependency to `build.gradle` in the `dependencies` block:

```kotlin
implementation("org.springframework.boot:spring-boot-starter-data-redis")
```

Edit the class `App` and rewrite the code as follows:

```kotlin
package urlshortener

import com.google.common.hash.Hashing
import org.apache.commons.validator.routines.UrlValidator
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import java.net.URI
import java.nio.charset.StandardCharsets
import jakarta.servlet.http.HttpServletRequest


@SpringBootApplication
class App

fun main(args: Array<String>) {
    runApplication<App>(*args)
}

@Controller
class RedirectController(
    private val sharedData: StringRedisTemplate
) {

    @GetMapping("/api/{id}")
    fun redirectTo(@PathVariable id: String) =
        sharedData.opsForValue()[id]?.let {
            ResponseEntity<Unit>(HttpHeaders().apply {
                location = URI.create(it)
            }, HttpStatus.TEMPORARY_REDIRECT)
        } ?: ResponseEntity(HttpStatus.NOT_FOUND)

    @PostMapping("/api", consumes = [MediaType.APPLICATION_FORM_URLENCODED_VALUE])
    fun shortener(@RequestParam url: String, req: HttpServletRequest): ResponseEntity<Unit> {
        val urlValidator = UrlValidator(arrayOf("http", "https"))
        return when {
            urlValidator.isValid(url) -> {
                val id = Hashing.murmur3_32_fixed().hashString(url, StandardCharsets.UTF_8).toString()
                sharedData.opsForValue()[id] = url
                ResponseEntity(HttpHeaders().apply {
                    location = URI.create(req.requestURL.append("/").append(id).toString())
                }, HttpStatus.CREATED)
            }
            else -> ResponseEntity(HttpStatus.BAD_REQUEST)
        }
    }
}
```

Note that we have moved the redirect endpoint to `/api/{id}` and `/api` and we had made explicit the parameter that the
shortener consumes.

Open a different terminal and then run:

```bash
$ cat <<EOF > redis.yml
version: '3'
services:
  redis:
    image: redis:alpine
    ports:
      - "6379:6379"
EOF

$ docker-compose -f redis.yml up
...
Creating urlshortener_redis_1 ... done
Attaching to urlshortener-redis-1
urlshortener-redis-1  | 1:C 06 Sep 2023 18:00:24.142 * oO0OoO0OoO0Oo Redis is starting oO0OoO0OoO0Oo
urlshortener-redis-1  | 1:C 06 Sep 2023 18:00:24.142 * Redis version=7.2.0, bits=64, commit=00000000, modified=0, pid=1, just started
...
```

Then run again the server. Now all your registered URI will be stored in your Redis instance.

Run the `urlshortener` and register a redirection.

```bash
http -v --form POST localhost:8080/api url=http://www.unizar.es/
```

The redirect is created at `6bb9db44`. Stops and restart the `urlshortener` and test.

```bash
http -v localhost:8080/api/6bb9db44
```

The `http://www.unizar.es/` is returned as `Location` value.

## Final remarks: Tests & Actuator endpoints

In this repo you will find the final version of the code plus unit and integration tests.
Testing requires to add as dependencies.

```kotlin
testImplementation("org.springframework.boot:spring-boot-starter-test")
testImplementation("org.apache.httpcomponents:httpclient")
// For integration tests
testImplementation("com.redis.testcontainers:testcontainers-redis-junit:1.6.2")
```

The classes for doing the tests are in the folder `app/src/main/test`.

### Unit Tests

For example `UnitTest` is able to test the `shortener` method by mocking the web server and the Redis storage.
Create a `UnitTest.kt` file:

```kotlin
package urlshortener

import org.hamcrest.Matchers.`is`
import org.junit.jupiter.api.Test
import org.mockito.BDDMockito.given
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.data.redis.core.ValueOperations
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.header
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@WebMvcTest(RedirectController::class)
class UnitTest {
    @MockBean
    private lateinit var valueOperations: ValueOperations<String, String>

    @MockBean
    private lateinit var stringRedisTemplate: StringRedisTemplate

    @Autowired
    private lateinit var mvc: MockMvc

    companion object {
        private const val HTTP_EXAMPLE_COM = "https://example.com/"
        private const val HASH = "83f94a17"
        private const val HASH_HTTP_EXAMPLE_COM = "http://localhost/api/$HASH"
    }
}
```

Inside we can add the unit test for the creation:

```kotlin
@Test
fun `should return 201 and a location in a valid redirection`() {
    given(stringRedisTemplate.opsForValue()).willReturn(valueOperations)
    mvc.perform(
        post("/api")
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .param("url", HTTP_EXAMPLE_COM)
    )
        .andExpect(status().isCreated)
        .andExpect(header().string("Location", `is`(HASH_HTTP_EXAMPLE_COM)))
}
```

And for the redirection:

```koltin
@Test
fun `should return 307 in a valid redirection`() {
    given(stringRedisTemplate.opsForValue()).willReturn(valueOperations)
    given(valueOperations[HASH]).willReturn(HTTP_EXAMPLE_COM)
    mvc.perform(
        get("/api/$HASH")
    )
        .andExpect(status().isTemporaryRedirect)
        .andExpect(header().string("Location", `is`(HTTP_EXAMPLE_COM)))
}
```

### Integration Tests

Meanwhile, `IntegrationTest.kt` it is a test that do the same with a running URL shortener and a Redis instance.

The base code is slightly different.
It includes the use of [Testcontainers](https://www.testcontainers.org/).
It is a Java library that supports JUnit tests, providing lightweight, throwaway instances of common databases, Selenium
web browsers, or anything else that can run in a Docker container.

```kotlin
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
}
```

Inside we can find the method for the integration test for the creation:

```kotlin
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
```

And the method for the redirection (that also creates a redirection):

```kotlin
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
```

### Actuator endpoints

[Actuator endpoints](https://spring.io/guides/gs/actuator-service/) let you monitor and interact with your application.
Just add:

```kotlin
implementation("org.springframework.boot:spring-boot-starter-actuator")
```

Spring Boot includes a number of built-in endpoints and lets you add your own.
For example, the `http://localhost:8080/actuator/health` endpoint 

```bash
http -v localhost:8080/actuator/health
```

Provides basic application health information.

```http
HTTP/1.1 200
Connection: keep-alive
Content-Type: application/vnd.spring-boot.actuator.v3+json
Date: Wed, 24 Aug 2022 16:56:11 GMT
Keep-Alive: timeout=60
Transfer-Encoding: chunked

{
    "status": "UP"
}
```

This can be tested by shutting down Redis. 
The endpoint will return a `DOWN` status.
