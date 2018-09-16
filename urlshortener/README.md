# Create a URL Shortener step-by-step
The goal of this tutorial is the creation of a powerful URL shortener with a few lines of Java.

## What is a URL shortener?
A URL shortener is a web service that makes a long URL short, easy to remember and to share. The behaviour of the application is as follows:

![URL shortener flow](https://github.com/UNIZAR-30246-WebEngineering/tutorials/blob/master/urlshortener/img/flow.png)

Rembember, the focus of this course is Web Engineering, therefore we will focus on the red interactions in the above figure.

## Prerequisites
Prerequisites:
- [Java SDK v1.8](http://www.java.com/en/) or higher.
- [Gradle 4.1](http://www.gradle.org/) or higher.
- [Redis 3.0](http://redis.io/download) or higher.
- [HTTPie](https://httpie.org/) or similar HTTP client for testing.
- [Sublime Text](https://www.sublimetext.com/) or similar as editor.

## Create the project stub

```bash
$ mkdir urlshortener
$ cd urlshortener
$ mkdir -p src/main/java/urlshortener
$ subl src/main/java/urlshortener/Application.java
```

Create the class `Application` with a `main` method that says hello world.

```bash
$ subl build.gradle
```

Copy the following code:

```groovy
apply plugin: 'application'
mainClassName = 'urlshortener.Application'
```

And then run:

```bash
$ gradle run
```

## Transform into a Spring Boot Web application

Edit the class `Application` and rewrite the code as follows:

```java
package urlshortener;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
```

And update `build.gradle` with the following code:

```groovy
buildscript {
    ext {
        springBootVersion = '2.0.5.RELEASE'
    }
    repositories {
        mavenCentral()
    }
    dependencies {
        classpath("org.springframework.boot:spring-boot-gradle-plugin:${springBootVersion}")
    }
}

// Enable Spring Boot helper tasks
apply plugin: 'org.springframework.boot'

// Enable the automatic management of some versions
apply plugin: 'io.spring.dependency-management'

// We are Java 8
apply plugin: 'java'
sourceCompatibility = 1.8
targetCompatibility = 1.8

repositories {
    mavenCentral()
}

dependencies {
    // Version is managed by io.spring.dependency-management
    compile "org.springframework.boot:spring-boot-starter-web"
}
```

And then run:

```bash
$ gradle bootRun
```

You have now a do-nothing web server listening at port 8080. Let's test it:

```bash
$ http -v localhost:8080
```

This is the client request (HTTPie):
```http
GET / HTTP/1.1
Accept: */*
Accept-Encoding: gzip, deflate
Connection: keep-alive
Host: localhost:8080
User-Agent: HTTPie/0.9.9

```

And this the server response (our `Application`):

```http
HTTP/1.1 404 
Content-Type: application/json;charset=UTF-8
Date: Sun, 17 Sep 2017 16:41:10 GMT
Transfer-Encoding: chunked

{
    "error": "Not Found",
    "message": "No message available",
    "path": "/",
    "status": 404,
    "timestamp": 1505666470380
}
```

This server can be killed with ```Ctrl-C```.

## URL Shortener version 0

Edit the class `Application` and rewrite the code as follows:

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
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @GetMapping(value = "/**")
    public void redirectTo(HttpServletRequest req, HttpServletResponse resp) {
        resp.sendRedirect(req.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE)
                             .toString().substring(1));
    }
}
```

You have now a redirecting endpoint at port 8080. Let's test it:

```
$ http -v localhost:8080/http://www.unizar.es/
```

This is the HTTP request.

```http
GET /http://www.unizar.es/ HTTP/1.1
Accept: */*
Accept-Encoding: gzip, deflate
Connection: keep-alive
Host: localhost:8080
User-Agent: HTTPie/0.9.9

```

This is the HTTP response.

```http
HTTP/1.1 302 
Content-Length: 0
Date: Sun, 17 Sep 2017 16:52:49 GMT
Location: http:/www.unizar.es/

```

## URL Shortener version 1

Edit the class `Application` and rewrite the code as follows:

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
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
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
$ http -v --form POST localhost:8080 url=http://www.unizar.es/
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
User-Agent: HTTPie/0.9.9

url=http%3A%2F%2Fwww.unizar.es%2F

```

This is the HTTP response.

```http
HTTP/1.1 201 
Content-Length: 0
Date: Sun, 17 Sep 2017 16:55:36 GMT
Location: http://localhost:8080/2108188503

```

Let's test the returned `Location`.

```bash
$ http -v localhost:8080/2108188503
```

This is the HTTP request.

```http
GET /2108188503 HTTP/1.1
Accept: */*
Accept-Encoding: gzip, deflate
Connection: keep-alive
Host: localhost:8080
User-Agent: HTTPie/0.9.9

```

This is the HTTP response.
```http
HTTP/1.1 307 
Content-Length: 0
Date: Sun, 17 Sep 2017 16:56:04 GMT
Location: http://www.unizar.es/

```

A request with a different value will raise an error.

```bash
$ http -v localhost:8080/3108188502
```

This is the HTTP request.

```http
GET /3108188502 HTTP/1.1
Accept: */*
Accept-Encoding: gzip, deflate
Connection: keep-alive
Host: localhost:8080
User-Agent: HTTPie/0.9.6

```

This is the HTTP response.

```http
HTTP/1.1 404 
Content-Length: 0
Date: Sun, 17 Sep 2017 16:56:50 GMT

```

## URL Shortener secured (version 2)

Add the following dependencies to `build.gradle` after `compile 'org.springframework.boot:spring-boot-starter-web'`:

```groovy
compile 'commons-validator:commons-validator:1.6'
compile 'com.google.guava:guava:23.0'   
```

Note that the version is not managed in these libraries. 
This happens because `guava` and `commons-validator` are not used by in the Spring Framework projects.

Edit the class `Application` and rewrite the code as follows:

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
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
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

Run `gradle compileJava`, then run again the server and test a bad request:

```bash
$ http -v --form POST localhost:8080 url=ftp://www.unizar.es/
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
User-Agent: HTTPie/0.9.9

url=ftp%3A%2F%2Fwww.unizar.es%2F

```

This is the HTTP response.
```http
HTTP/1.1 400 
Connection: close
Content-Length: 0
Date: Sun, 17 Sep 2017 17:06:11 GMT

```

## Scalable URL Shortener (version 3)

Add the following dependency to `build.gradle` after `compile 'com.google.guava:guava:19.0' `:

```groovy
compile 'org.springframework.boot:spring-boot-starter-data-redis'
```

Edit the class `Application` and rewrite the code as follows:

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
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Autowired
    private StringRedisTemplate sharedData;

    @GetMapping(value = "/{id}")
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

    @PostMapping
    public ResponseEntity<String> shortener(@RequestParam MultiValueMap<String, String> form, HttpServletRequest req) {
        String url = form.getFirst("url");
        UrlValidator urlValidator = new UrlValidator(new String[]{"http", "https"});
        if (url != null && urlValidator.isValid(url)) {
            String id = Hashing.murmur3_32().hashString(url, StandardCharsets.UTF_8).toString();
            sharedData.opsForValue().set(id, url);
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

Ensure that your Redis instance is running. 

```bash
$ redis-server /usr/local/etc/redis.conf
                _._                                                  
           _.-``__ ''-._                                             
      _.-``    `.  `_.  ''-._           Redis 3.2.3 (00000000/0) 64 bit
  .-`` .-```.  ```\/    _.,_ ''-._                                   
 (    '      ,       .-`  | `,    )     Running in standalone mode
 |`-._`-...-` __...-.``-._|'` _.-'|     Port: 6379
 |    `-._   `._    /     _.-'    |     PID: 64746
  `-._    `-._  `-./  _.-'    _.-'                                   
 |`-._`-._    `-.__.-'    _.-'_.-'|                                  
 |    `-._`-._        _.-'_.-'    |           http://redis.io        
  `-._    `-._`-.__.-'_.-'    _.-'                                   
 |`-._`-._    `-.__.-'    _.-'_.-'|                                  
 |    `-._`-._        _.-'_.-'    |                                  
  `-._    `-._`-.__.-'_.-'    _.-'                                   
      `-._    `-.__.-'    _.-'                                       
          `-._        _.-'                                           
              `-.__.-'                                               

64746:M 17 Sep 17:20:25.102 # Server started, Redis version 3.2.3
64746:M 17 Sep 17:20:25.103 * DB loaded from disk: 0.001 seconds
64746:M 17 Sep 17:20:25.103 * The server is now ready to accept connections on port 6379
...
```

Or if you have [Docker](https://www.docker.com/) installed open a different terminal and then run:

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
Creating network "docker_default" with the default driver
Creating docker_redis_1 ... 
Creating docker_redis_1 ... done
Attaching to docker_redis_1
redis_1  | 1:C 18 Sep 16:33:40.313 # oO0OoO0OoO0Oo Redis is starting oO0OoO0OoO0Oo
redis_1  | 1:C 18 Sep 16:33:40.313 # Redis version=4.0.1, bits=64, commit=00000000, modified=0, pid=1, just started
...
```

Now all your registered URI will stored in your Redis instance. You can run the tests again. 

## Final remarks: Tests & Actuator endpoints

In this repo you will find the final version of the code plus unit and integration tests. 
Testing requires to add as dependencies after `compile 'org.springframework.boot:spring-boot-starter-data-redis'`

```groovy
testCompile 'org.springframework.boot:spring-boot-starter-test'
testCompile 'org.apache.httpcomponents:httpclient'
```

The classes for doing the tests are in the folder `src/main/test`.

### Unit Tests

For example `urlshortener.UnitTest` is able to test the `shortener` method by mocking the web server and the redis storage. 
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
@WebMvcTest(Application.class)
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
private static final String HASH_HTTP_EXAMPLE_COM = "http://localhost/"+HASH;

@Test
public void testCreation() throws Exception {
	given(stringRedisTemplate.opsForValue()).willReturn(valueOperations);
	this.mvc.perform(post("/")
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
	this.mvc.perform(get("/"+HASH)).
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
private static final UriComponents LOCATION = UriComponentsBuilder.fromUriString("http://localhost:{port}/{hash}").build();

@Test
public void testCreation() throws Exception {
    MultiValueMap<String, Object> parts = new LinkedMultiValueMap<>();
    parts.add("url", HTTP_EXAMPLE_COM);
    ResponseEntity<String> response = restTemplate.postForEntity("/", parts, String.class);
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
	ResponseEntity<String> created = restTemplate.postForEntity("/", parts, String.class);
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
compile 'org.springframework.boot:spring-boot-starter-actuator'
```
Spring Boot includes a number of built-in endpoints and lets you add your own. 
For example, the `http://localhost:8080/actuator/health` endpoint provides basic application health information.

