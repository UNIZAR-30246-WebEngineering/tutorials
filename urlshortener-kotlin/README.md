# Create a URL Shortener step-by-step

The goal of this tutorial is the creation of a powerful URL shortener with a few lines of Kotlin.

## What is a URL shortener

A URL shortener is a web service that makes a long URL short, easy to remember and to share. The behaviour of the application is as follows:

![URL shortener flow](https://github.com/UNIZAR-30246-WebEngineering/tutorials/blob/master/urlshortener-kotlin/img/flow.png)

Rembember, the focus of this course is Web Engineering, therefore we will focus on the red interactions in the above figure.

## Prerequisites

Prerequisites:

- [Java SDK v1.8](http://www.java.com/en/) or higher.
- [Gradle Build Tool 6.0](http://www.gradle.org/) or higher.
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
  5: Swift
Enter selection (default: Java) [1..5] 4

Select build script DSL:
  1: Groovy
  2: Kotlin
Enter selection (default: Groovy) [1..2] 2

Project name (default: urlshortener):
Source package (default: urlshortener):
```

And then run:

```bash
gradle run
```

## Transform into a Spring Boot Web application

Update `build.gradle.kts` by replacing in the `plugins` block by:

```kotlin
plugins {
    kotlin("jvm") version "1.3.72"
    kotlin("plugin.spring") version "1.3.72"
    id("org.springframework.boot") version "2.3.3.RELEASE"
    id("io.spring.dependency-management") version "1.0.10.RELEASE"
}
```
in the `dependencies` block add:

```kotlin
implementation("org.springframework.boot:spring-boot-starter-web")
```
remove the `application` block, and add at the end

```kotlin
tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "1.8"
    }
}
```

Edit the class `App` at `src/main/kotlin/urlshortener/App.kt` and rewrite the code as follows:

```kotlin
package urlshortener

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class App

fun main(args: Array<String>) {
    runApplication<App>(*args)
}
```

Remove the file  `src/test/kotlin/urlshortener/AppTest.kt`.

```bash
rm src/test/kotlin/urlshortener/AppTest.kt
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
User-Agent: HTTPie/2.2.0

```

And this the server response (our `App`):

```http
HTTP/1.1 404 
Connection: keep-alive
Content-Type: application/json
Date: Mon, 14 Sep 2020 14:41:35 GMT
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
    "timestamp": "2020-09-14T14:41:35.168+00:00"
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
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@SpringBootApplication
class App

fun main(args: Array<String>) {
    runApplication<App>(*args)
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
User-Agent: HTTPie/2.2.0

```

This is the HTTP response:

```http
HTTP/1.1 302 
Connection: keep-alive
Content-Length: 0
Date: Mon, 14 Sep 2020 14:44:32 GMT
Keep-Alive: timeout=60
Location: http:/www.unizar.es/

```

## URL Shortener version 1

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
import javax.servlet.http.HttpServletRequest


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
            ResponseEntity<Void>(HttpHeaders().apply {
                location = URI.create(it)
            }, HttpStatus.TEMPORARY_REDIRECT)
        } ?: ResponseEntity(HttpStatus.NOT_FOUND)

    @PostMapping
    fun shortener(@RequestParam form: MultiValueMap<String?, String?>, req: HttpServletRequest): ResponseEntity<Void> {
        val url = form.getFirst("url") ?: return ResponseEntity(HttpStatus.BAD_REQUEST)
        val id = url.hashCode().toString()
        sharedData[id] = url
        return ResponseEntity(HttpHeaders().apply { 
          location = URI.create(req.requestURL.append(id).toString()) 
         }, HttpStatus.CREATED)
    }
}
```

Run it now and you have a working shortener endpoint at port 8080. Let's test it:

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
User-Agent: HTTPie/2.2.0

url=http%3A%2F%2Fwww.unizar.es%2F
```

This is the HTTP response.

```http
HTTP/1.1 201 
Connection: keep-alive
Content-Length: 0
Date: Mon, 14 Sep 2020 14:57:44 GMT
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
User-Agent: HTTPie/2.2.0

```

This is the HTTP response.

```http
HTTP/1.1 307 
Connection: keep-alive
Content-Length: 0
Date: Mon, 14 Sep 2020 14:58:22 GMT
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
User-Agent: HTTPie/2.2.0

```

This is the HTTP response.

```http
HTTP/1.1 404 
Connection: keep-alive
Content-Length: 0
Date: Mon, 14 Sep 2020 14:58:52 GMT
Keep-Alive: timeout=60

```

## URL Shortener secured (version 2)

Add the following dependencies to `build.gradle.kts` in the `dependencies` block:

```kotlin
    implementation("commons-validator:commons-validator:1.6")
    implementation("com.google.guava:guava:23.0")
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
import javax.servlet.http.HttpServletRequest


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
            ResponseEntity<Void>(HttpHeaders().apply {
                location = URI.create(it)
            }, HttpStatus.TEMPORARY_REDIRECT)
        } ?: ResponseEntity(HttpStatus.NOT_FOUND)

    @PostMapping
    fun shortener(@RequestParam form: MultiValueMap<String?, String?>, req: HttpServletRequest): ResponseEntity<Void> {
        val url = form.getFirst("url")
        val urlValidator = UrlValidator(arrayOf("http", "https"))
        return when {
            url == null -> ResponseEntity(HttpStatus.BAD_REQUEST)
            urlValidator.isValid(url) -> {
                val id = Hashing.murmur3_32().hashString(url, StandardCharsets.UTF_8).toString()
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
User-Agent: HTTPie/2.2.0

url=ftp%3A%2F%2Fwww.unizar.es%2F

```

This is the HTTP response.

```http
HTTP/1.1 400 
Connection: close
Content-Length: 0
Date: Mon, 14 Sep 2020 15:05:05 GMT

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
import javax.servlet.http.HttpServletRequest


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
            ResponseEntity<Void>(HttpHeaders().apply {
                location = URI.create(it)
            }, HttpStatus.TEMPORARY_REDIRECT)
        } ?: ResponseEntity(HttpStatus.NOT_FOUND)

    @PostMapping("/api", consumes = [MediaType.APPLICATION_FORM_URLENCODED_VALUE])
    fun shortener(@RequestParam url: String, req: HttpServletRequest): ResponseEntity<Void> {
        val urlValidator = UrlValidator(arrayOf("http", "https"))
        return when {
            urlValidator.isValid(url) -> {
                val id = Hashing.murmur3_32().hashString(url, StandardCharsets.UTF_8).toString()
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

Note that we have moved the redirect endpoint to `/api/{id}` and `/api` and we have explicited the parameter that the shortener consumes.

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
Attaching to urlshortener_redis_1
redis_1  | 1:C 18 Sep 2019 14:44:48.313 # oO0OoO0OoO0Oo Redis is starting oO0OoO0OoO0Oo
redis_1  | 1:C 18 Sep 2019 14:44:48.313 # Redis version=5.0.5, bits=64, commit=00000000, modified=0, pid=1, just started
red
...
```

Then run again the server. Now all your registered URI will stored in your Redis instance.

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
```

The classes for doing the tests are in the folder `src/main/test`.

### Unit Tests

For example `urlshortener.UnitTest` is able to test the `shortener` method by mocking the web server and the Redis storage.
The class that contains the test is as follows:

```kotlin
package urlshortener

import org.hamcrest.Matchers.`is`
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.BDDMockito.given
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.data.redis.core.ValueOperations
import org.springframework.http.MediaType
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.header
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@RunWith(SpringRunner::class)
@WebMvcTest(RedirectController::class)
class UnitTest {
    @MockBean
    private lateinit var valueOperations: ValueOperations<String, String>

    @MockBean
    private lateinit var stringRedisTemplate: StringRedisTemplate

    @Autowired
    private lateinit var mvc: MockMvc

    companion object {
        const val HTTP_EXAMPLE_COM = "http://example.com/"
        const val HASH = "f684a3c4"
        const val HASH_HTTP_EXAMPLE_COM = "http://localhost/api/$HASH"
    }
}
```

Inside we can add the unit test for the creation:

```kotlin
@Test
fun testCreation() {
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
fun testRedirection() {
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

Meanwhile in  `urlshortener.IntegrationTest` it is a test that do the same with a running URL shortener and a Redis instance.

The base code is slightly different:

```kotlin
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

}
```

Inside we can find the method for the integration test for the creation:

```kotlin
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
```

And the method for the redirection (that also creates a redirection):

```kotlin
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
```

### Actuator endpoints

[Actuator endpoints](https://spring.io/guides/gs/actuator-service/) let you monitor and interact with your application.
Just add:

```kotlin
implementation("org.springframework.boot:spring-boot-starter-actuator")
```

Spring Boot includes a number of built-in endpoints and lets you add your own.
For example, the `http://localhost:8080/actuator/health` endpoint provides basic application health information.

```http
HTTP/1.1 200
Connection: keep-alive
Content-Type: application/vnd.spring-boot.actuator.v3+json
Date: Mon, 14 Sep 2020 15:33:56 GMT
Keep-Alive: timeout=60
Transfer-Encoding: chunked

{
    "status": "UP"
}
```

### Documentation

[Swagger](https://swagger.io/) can create automatically a readable documentation of the API.
Just add the following dependency.

```groovy
implementation("io.springfox:springfox-boot-starter:3.0.0")
```
Now you can go to `http://localhost:8080/v2/api-docs` and obtain a JSON document that describes the API.

A HTML version of the documentation ([Swagger UI](https://swagger.io/tools/swagger-ui/)) is now available at `http://localhost:8080/swagger-ui/index.html`.

Each API operation (`POST /api`, `GET /api/{ip}`) can be tried out.
Note that redirects (`GET /api/{ip}`) produces a [CORS](https://en.wikipedia.org/wiki/Cross-origin_resource_sharing) error
in the browser because Swagger UI does not perform requests in [`no-cors` mode](https://developer.mozilla.org/en-US/docs/Web/API/Request/mode).
