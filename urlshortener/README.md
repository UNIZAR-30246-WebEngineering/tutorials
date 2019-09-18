# Create a URL Shortener step-by-step

The goal of this tutorial is the creation of a powerful URL shortener with a few lines of Java.

## What is a URL shortener

A URL shortener is a web service that makes a long URL short, easy to remember and to share. The behaviour of the application is as follows:

![URL shortener flow](https://github.com/UNIZAR-30246-WebEngineering/tutorials/blob/master/urlshortener/img/flow.png)

Rembember, the focus of this course is Web Engineering, therefore we will focus on the red interactions in the above figure.

## Prerequisites

Prerequisites:

- [Java SDK v1.8](http://www.java.com/en/) or higher.
- [Gradle Build Tool 5.6](http://www.gradle.org/) or higher.
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
Enter selection (default: Java) [1..5]

Select build script DSL:
  1: Groovy
  2: Kotlin
Enter selection (default: Groovy) [1..2]

Select test framework:
  1: JUnit 4
  2: TestNG
  3: Spock
  4: JUnit Jupiter
Enter selection (default: JUnit 4) [1..4]

Project name (default: urlshortener):
Source package (default: urlshortener):
```

And then run:

```bash
gradle run
```

## Transform into a Spring Boot Web application

Edit the class `App` at `src/main/java/urlshortener/App.java` and rewrite the code as follows:

```java
package urlshortener;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class App {
    public static void main(String[] args) {
        SpringApplication.run(App.class, args);
    }
}
```

Update `build.gradle` by adding in the `plugins` block:

```groovy
id 'org.springframework.boot' version '2.1.8.RELEASE'
id 'io.spring.dependency-management' version '1.0.8.RELEASE'
```

and in the `dependencies` block:

```groovy
implementation 'org.springframework.boot:spring-boot-starter-web'
```

Remove the file  `src/test/java/urlshortener/AppTest.java`.

```bash
rm src/test/java/urlshortener/AppTest.java
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
User-Agent: HTTPie/1.9.2

```

And this the server response (our `App`):

```http
HTTP/1.1 404
Content-Type: application/json;charset=UTF-8
Date: Wed, 18 Sep 2019 14:27:01 GMT
Transfer-Encoding: chunked

{
    "error": "Not Found",
    "message": "No message available",
    "path": "/",
    "status": 404,
    "timestamp": "2019-09-18T14:27:01.897+0000"
}
```

This server can be killed with ```Ctrl-C```.

## URL Shortener version 0

Edit the class `App` and rewrite the code as follows:

```Java
package urlshortener;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.HandlerMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@SpringBootApplication
@Controller
public class App {
    public static void main(String[] args) {
        SpringApplication.run(App.class, args);
    }

    @GetMapping(value = "/**")
    public void redirectTo(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.sendRedirect(req.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE)
                             .toString().substring(1));
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
User-Agent: HTTPie/1.0.2

```

This is the HTTP response:

```http
HTTP/1.1 302
Content-Length: 0
Date: Wed, 18 Sep 2019 14:31:02 GMT
Location: http:/www.unizar.es/

```

## URL Shortener version 1

Edit the class `App` and rewrite the code as follows:

```Java
package urlshortener;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

@SpringBootApplication
@Controller
public class App {
    public static void main(String[] args) {
        SpringApplication.run(App.class, args);
    }

    private Map<String, String> sharedData = new HashMap<>();

    @GetMapping(value = "/{id}")
    public ResponseEntity<Void> redirectTo(@PathVariable String id) {
        String key = sharedData.get(id);
        if (key != null) {
            HttpHeaders responseHeaders = new HttpHeaders();
            responseHeaders.setLocation(URI.create(key));
            return new ResponseEntity<>(responseHeaders, HttpStatus.TEMPORARY_REDIRECT);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping
    public ResponseEntity<String> shortener(@RequestParam MultiValueMap<String, String> form, HttpServletRequest req)  {
        String url = form.getFirst("url");
        String id = "" + url.hashCode();
        sharedData.put(id, url);
        URI location = URI.create(req.getRequestURL().append(id).toString());
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.setLocation(location);
        return new ResponseEntity<>(responseHeaders, HttpStatus.CREATED);
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
User-Agent: HTTPie/1.0.2

url=http%3A%2F%2Fwww.unizar.es%2F

```

This is the HTTP response.

```http
HTTP/1.1 201
Content-Length: 0
Date: Wed, 18 Sep 2019 14:35:02 GMT
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
User-Agent: HTTPie/1.0.2

```

This is the HTTP response.

```http
HTTP/1.1 307
Content-Length: 0
Date: Wed, 18 Sep 2019 14:35:48 GMT
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
User-Agent: HTTPie/1.0.2

```

This is the HTTP response.

```http
HTTP/1.1 404
Content-Length: 0
Date: Wed, 18 Sep 2019 14:36:18 GMT

```

## URL Shortener secured (version 2)

Add the following dependencies to `build.gradle` in the `dependencies` block:

```groovy
implementation 'commons-validator:commons-validator:1.6'
implementation 'com.google.guava:guava:23.0'
```

Note that the version is not managed in these libraries.

This happens because `guava` and `commons-validator` are not used in the Spring Framework projects.

Edit the class `App` and rewrite the code as follows:

```java
package urlshortener;

import com.google.common.hash.Hashing;
import org.apache.commons.validator.routines.UrlValidator;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@SpringBootApplication
@Controller
public class App {
    public static void main(String[] args) {
        SpringApplication.run(App.class, args);
    }

    private Map<String, String> sharedData = new HashMap<>();

    @GetMapping(value = "/{id}")
    public ResponseEntity<Void> redirectTo(@PathVariable String id) throws IOException {
        String key = sharedData.get(id);
        if (key != null) {
            HttpHeaders responseHeaders = new HttpHeaders();
            responseHeaders.setLocation(URI.create(key));
            return new ResponseEntity<>(responseHeaders, HttpStatus.TEMPORARY_REDIRECT);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping
    public ResponseEntity<Void> shortener(@RequestParam MultiValueMap<String, String> form, HttpServletRequest req) {
        String url = form.getFirst("url");
        UrlValidator urlValidator = new UrlValidator(new String[]{"http", "https"});
        if (url != null && urlValidator.isValid(url)) {
            String id = Hashing.murmur3_32().hashString(url, StandardCharsets.UTF_8).toString();
            sharedData.put(id, url);
            URI location = URI.create(req.getRequestURL().append(id).toString());
            HttpHeaders responseHeaders = new HttpHeaders();
            responseHeaders.setLocation(location);
            return new ResponseEntity<>(responseHeaders, HttpStatus.CREATED);
        } else {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
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
User-Agent: HTTPie/1.0.2

url=ftp%3A%2F%2Fwww.unizar.es%2F

```

This is the HTTP response.

```http
HTTP/1.1 400
Connection: close
Content-Length: 0
Date: Wed, 18 Sep 2019 14:39:03 GMT

```

## Scalable URL Shortener (version 3)

Add the following dependency to `build.gradle` in the `dependencies` block:

```groovy
implementation 'org.springframework.boot:spring-boot-starter-data-redis'
```

Edit the class `App` and rewrite the code as follows:

```java
package urlshortener;

import com.google.common.hash.Hashing;
import org.apache.commons.validator.routines.UrlValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;

@SpringBootApplication
@Controller
public class App {
    public static void main(String[] args) {
        SpringApplication.run(App.class, args);
    }

    @Autowired
    private StringRedisTemplate sharedData;

    @GetMapping(value = "/api/{id}")
    public ResponseEntity<Void> redirectTo(@PathVariable String id) {
        String key = sharedData.opsForValue().get(id);
        if (key != null) {
            HttpHeaders responseHeaders = new HttpHeaders();
            responseHeaders.setLocation(URI.create(key));
            return new ResponseEntity<>(responseHeaders, HttpStatus.TEMPORARY_REDIRECT);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping("/api")
    public ResponseEntity<String> shortener(@RequestParam("url") String url, HttpServletRequest req) {
        UrlValidator urlValidator = new UrlValidator(new String[]{"http", "https"});
        if (url != null && urlValidator.isValid(url)) {
            String id = Hashing.murmur3_32().hashString(url, StandardCharsets.UTF_8).toString();
            sharedData.opsForValue().set(id, url);
            URI location = URI.create(req.getRequestURL().append("/"+id).toString());
            HttpHeaders responseHeaders = new HttpHeaders();
            responseHeaders.setLocation(location);
            return new ResponseEntity<>(responseHeaders, HttpStatus.CREATED);
        } else {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }
}
```

Note that we have moved the redirect endpoint to `/api/{id}` and `/api`.

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

```groovy
testImplementation 'org.springframework.boot:spring-boot-starter-test'
testImplementation 'org.apache.httpcomponents:httpclient'
```

The classes for doing the tests are in the folder `src/main/test`.

### Unit Tests

For example `urlshortener.UnitTest` is able to test the `shortener` method by mocking the web server and the Redis storage.
The class that contains the test is as follows:

```java
package urlshortener;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.is;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@WebMvcTest(App.class)
public class UnitTest {

    @MockBean
    private ValueOperations<String, String> valueOperations;

    @MockBean
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private MockMvc mvc;
}
```

Inside we can find the unit test for the creation:

```java
private static final String HTTP_EXAMPLE_COM = "http://example.com/";
private static final String HASH = "f684a3c4";
private static final String HASH_HTTP_EXAMPLE_COM = "http://localhost/api/"+HASH;

@Test
public void testCreation() throws Exception {
    given(stringRedisTemplate.opsForValue()).willReturn(valueOperations);
    this.mvc.perform(post("/api")
            .contentType(MediaType.APPLICATION_FORM_URLENCODED).param("url", HTTP_EXAMPLE_COM)).
            andExpect(status().isCreated()).
            andExpect(header().string("Location", is(HASH_HTTP_EXAMPLE_COM)));
}
```

And for the redirection:

```java
@Test
public void testRedirection() throws Exception {
    given(stringRedisTemplate.opsForValue()).willReturn(valueOperations);
    given(valueOperations.get(HASH)).willReturn(HTTP_EXAMPLE_COM);
    this.mvc.perform(get("/api/"+HASH)).
            andExpect(status().isTemporaryRedirect()).
            andExpect(header().string("Location", is(HTTP_EXAMPLE_COM)));
}
```

### Integration Tests

Meanwhile in  `urlshortener.IntegrationTest` it is a test that do the same with a running URL shortener and a Redis instance.

The base code is slightly different:

```java
package urlshortener;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment= RANDOM_PORT)
public class IntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @LocalServerPort
    private int port;

}
```

Inside we can find the method for the integration test for the creation:

```java
private static final String HTTP_EXAMPLE_COM = "http://example.com/";
private static final String HASH_HTTP_EXAMPLE_COM = "f684a3c4";
private static final UriComponents LOCATION = UriComponentsBuilder.fromUriString("http://localhost:{port}/api/{hash}").build();

@Test
public void testCreation() throws Exception {
    MultiValueMap<String, Object> parts = new LinkedMultiValueMap<>();
    parts.add("url", HTTP_EXAMPLE_COM);
    ResponseEntity<String> response = restTemplate.postForEntity("/api", parts, String.class);
    assertThat(response.getStatusCode(), is(HttpStatus.CREATED));
    Map<String, Object> components = new HashMap<>();
    components.put("port", port);
    components.put("hash", HASH_HTTP_EXAMPLE_COM);
    assertThat(response.getHeaders().getLocation(), is(LOCATION.expand(components).toUri()));
}
```

And the method for the redirection (that also creates a redirection):

```java
@Test
public void testRedirection() throws Exception {
    MultiValueMap<String, Object> parts = new LinkedMultiValueMap<>();
    parts.add("url", HTTP_EXAMPLE_COM);
    ResponseEntity<String> created = restTemplate.postForEntity("/api", parts, String.class);
    assertThat(created.getHeaders().getLocation(), is(notNullValue()));
    String path = created.getHeaders().getLocation().getPath();
    ResponseEntity<String> response = restTemplate.getForEntity(path, String.class);
    assertThat(response.getStatusCode(), is(HttpStatus.TEMPORARY_REDIRECT));
    assertThat(response.getHeaders().getLocation(), is(new URI(HTTP_EXAMPLE_COM)));
}
```

### Actuator endpoints

[Actuator endpoints](https://spring.io/guides/gs/actuator-service/) let you monitor and interact with your application.
Just add:

```groovy
implementation 'org.springframework.boot:spring-boot-starter-actuator'
```

Spring Boot includes a number of built-in endpoints and lets you add your own.
For example, the `http://localhost:8080/actuator/health` endpoint provides basic application health information.

```http
HTTP/1.1 200
Content-Type: application/vnd.spring-boot.actuator.v2+json;charset=UTF-8
Date: Wed, 18 Sep 2019 15:04:53 GMT
Transfer-Encoding: chunked

{
    "status": "UP"
}
```

### Documentation

[Swagger](https://swagger.io/) can create automatically a readable documentation of the API.
Just add the following dependency.

```groovy
implementation 'io.springfox:springfox-swagger2:2.9.2'
```

Next, annotate the `App` class with `@EnableSwagger2` and add the following bean:

```java
@Bean
public Docket api() {
    return new Docket(DocumentationType.SWAGGER_2)  
        .select()
        .apis(RequestHandlerSelectors.any())
        .paths(PathSelectors.any())
        .build();
}

Now you can go to `http://localhost:8080/v2/api-docs` and obtain a JSON document that describes the API.

A human readable view of the documentation can be produced by adding the follwing dependency:

```groovy
implementation 'io.springfox:springfox-swagger-ui:2.9.2'
```

A HTML version of the documentation ([Swagger UI](https://swagger.io/tools/swagger-ui/))is now available at `http://localhost:8080/swagger-ui.html`.

Each API operation (`POST /api`, `GET /api/{ip}`) can be tried out.
Note that redirects (`GET /api/{ip}`) produces a [CORS](https://en.wikipedia.org/wiki/Cross-origin_resource_sharing) error
in the browser because Swagger UI does not perform request in [`no-cors` mode](https://developer.mozilla.org/en-US/docs/Web/API/Request/mode).
