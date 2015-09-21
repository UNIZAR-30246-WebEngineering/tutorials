# Create a URL Shortener step-by-step
The goal of this tutorial is the creation of a really short URL shortener in java. 
## Prerequisite
Prerequisites:
- [Java SDK v1.8](http://www.java.com/en/) or higher.
- [Gradle 2.6](http://www.gradle.org/) or higher.
- [Redis 3.0](http://redis.io/download).
- [cURL](http://curl.haxx.se/) or similar for testing.
- [Sublime Text](https://www.sublimetext.com/) or similar as editor.

## Create the project stub
```
$ mkdir urlshortener
$ cd urlshortener
$ mkdir -p src/main/java
$ subl src/main/java//Application.java
```
Create the class ```Application``` with a ```main``` method that says hello world.
```
$ subl build.gradle
```
Copy the following code:
```Groovy
apply plugin:'application'
mainClassName = 'urlshortener.Application'
```
And then run:
```
$ gradle run
```
## Transform into a spring-boot Web application
Edit the class ```Application``` and rewrite the code as follows:
```Java
package urlshortener;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.SpringApplication;
@SpringBootApplication
public class Application {
	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}
}
```
And update ```build.gradle``` with the following code:
```Groovy
buildscript {
    ext {
        springVersion = '1.2.6.RELEASE'
    }
    repositories {
        mavenCentral()
        maven { url "http://repo.spring.io/release" }
    }
    dependencies {
        classpath "org.springframework.boot:spring-boot-gradle-plugin:$springVersion"
    }
}
apply plugin: 'java'
apply plugin: 'spring-boot'

sourceCompatibility = 1.8
targetCompatibility = 1.8

repositories {
    mavenCentral()
    maven { url 'http://repo.spring.io/release' }
}
dependencies {
    compile "org.springframework.boot:spring-boot-starter-web"
}
```
And then run:
```
$ gradle run
```
You have now a do-nothing web server listening at port 8080. Let's test it:
```
$ curl -v localhost:8080
> GET / HTTP/1.1
> User-Agent: curl/7.30.0
> Host: localhost:8080
> Accept: */*
>
f< HTTP/1.1 404 Not Found
< Server: Apache-Coyote/1.1
< Content-Type: application/json;charset=UTF-8
< Transfer-Encoding: chunked
< Date: Wed, 17 Sep 2014 23:41:52 GMT
<
{"timestamp":1410997312496,"status":404,"error":"Not Found","message":"","path":"/"}
```
This server can be stoped with ```Ctrl-Z```
## URL Shortener version 0
Edit the class ```UrlShortener``` and rewrite the code as follows:
```Java
package urlshortener;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.SpringApplication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.*;
import java.io.IOException;
import javax.servlet.http.*;
@SpringBootApplication
@Controller
public class Application {
	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}
	@RequestMapping(value="/**", method = RequestMethod.GET)
	public void redirectTo(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		resp.sendRedirect(req.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE).toString().substring(1));
	}
}
```
You have now a redirecting enpoint at port 8080. Let's test it:
```
$ curl -v localhost:8080/http://www.unizar.es/
url -v localhost:8080/http://www.unizar.es/
> GET /http://www.unizar.es/ HTTP/1.1
> User-Agent: curl/7.30.0
> Host: localhost:8080
> Accept: */*
>
< HTTP/1.1 302 Found
< Server: Apache-Coyote/1.1
< Location: http:/www.unizar.es/
< Content-Length: 0
< Date: Thu, 18 Sep 2014 00:05:56 GMT
<
```
## URL Shortener version 1
Edit the class ```Application``` and rewrite the code as follows:
```Java
package urlshortener;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.SpringApplication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.*;
import java.io.IOException;
import javax.servlet.http.*;
import org.springframework.http.*;
import org.springframework.util.MultiValueMap;
import java.util.*;
@SpringBootApplication
@Controller
public class Application {
	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}
	private Map<String,String> sharedData = new HashMap<String,String>();
	@RequestMapping(value="/{id}", method = RequestMethod.GET)
	public void redirectTo(@PathVariable String id, HttpServletResponse resp) throws IOException {
		String key = sharedData.get(id);
		if (key != null) {
			resp.sendRedirect(key);
		} else {
			resp.sendError(HttpServletResponse.SC_NOT_FOUND);
		}
	}
	@RequestMapping(method = RequestMethod.POST)
	public ResponseEntity<String> shortener(@RequestParam MultiValueMap<String,String> form, HttpServletRequest req) throws IOException {
		String url = form.getFirst("url");
		String id = ""+url.hashCode();
		sharedData.put(id, url);
		return new ResponseEntity<String>(req.getRequestURL().append(id+"\n").toString(), HttpStatus.CREATED);
	}
}
```
Run it now and you have a working shortener enpoint at port 8080. Let's test it:
```
$ curl -v -d "url=http://www.unizar.es/" -X POST http://localhost:8080
> POST / HTTP/1.1
> User-Agent: curl/7.30.0
> Host: localhost:8080
> Accept: */*
> Content-Length: 25
> Content-Type: application/x-www-form-urlencoded
>
* upload completely sent off: 25 out of 25 bytes
< HTTP/1.1 201 Created
< Server: Apache-Coyote/1.1
< Content-Type: text/plain;charset=ISO-8859-1
< Content-Length: 33
< Date: Thu, 18 Sep 2014 00:18:39 GMT
<
http://localhost:8080/2108188503
$ curl -v localhost:8080/2108188503
> GET /2108188503 HTTP/1.1
> User-Agent: curl/7.30.0
> Host: localhost:8080
> Accept: */*
>
< HTTP/1.1 302 Found
< Server: Apache-Coyote/1.1
< Location: http://www.unizar.es/
< Content-Length: 0
< Date: Thu, 18 Sep 2014 00:21:05 GMT
<
```
## URL Shortener secured (version 2)
Add the following dependencies to ```build.gradle```:
```Groovy
compile 'commons-validator:commons-validator:1.4.0'
compile 'com.google.guava:guava:17.0'    
```
Edit the class ```Application``` and rewrite the code as follows:
```Java
package urlshortener;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.SpringApplication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.*;
import java.io.IOException;
import javax.servlet.http.*;
import org.springframework.http.*;
import org.springframework.util.MultiValueMap;
import java.util.*;
import java.nio.charset.StandardCharsets;
import com.google.common.hash.Hashing;
import org.apache.commons.validator.routines.UrlValidator;
@SpringBootApplication
@Controller
public class Application {
	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}
	private Map<String,String> sharedData = new HashMap<String,String>();
	@RequestMapping(value="/{id}", method = RequestMethod.GET)
	public void redirectTo(@PathVariable String id, HttpServletResponse resp) throws IOException {
		String key = sharedData.get(id);
		if (key != null) {
			resp.sendRedirect(key);
		} else {
			resp.sendError(HttpServletResponse.SC_NOT_FOUND);
		}
	}
	@RequestMapping(method = RequestMethod.POST)
	public ResponseEntity<String> shortener(@RequestParam MultiValueMap<String,String> form, HttpServletRequest req) throws IOException {
		String url = form.getFirst("url");
		UrlValidator urlValidator = new UrlValidator(new String[]{"http", "https"});
		if (urlValidator.isValid(url)) {
			String id = Hashing.murmur3_32().hashString(url, StandardCharsets.UTF_8).toString();
			sharedData.put(id, url);
			return new ResponseEntity<String>(req.getRequestURL().append(id+"\n").toString(), HttpStatus.CREATED);
		} else {
			return new ResponseEntity<String>(HttpStatus.BAD_REQUEST);
		}
	}
}
```
Run and test a bad request:
```
curl -v -d "url=ftp://www.unizar.es/" -X POST http://localhost:8080
> POST / HTTP/1.1
> User-Agent: curl/7.30.0
> Host: localhost:8080
> Accept: */*
> Content-Length: 24
> Content-Type: application/x-www-form-urlencoded
>
* upload completely sent off: 24 out of 24 bytes
< HTTP/1.1 400 Bad Request
< Server: Apache-Coyote/1.1
< Content-Length: 0
< Date: Thu, 18 Sep 2014 00:29:18 GMT
< Connection: close
<
```
## Scalable URL Shortener (version 3)
Add the following dependency to ```build.gradle```:
```groovy
compile "org.springframework.boot:spring-boot-starter-redis"
```
Edit the class ```Application``` and rewrite the code as follows:
```java
package urlshortener;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.SpringApplication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.*;
import java.io.IOException;
import javax.servlet.http.*;
import org.springframework.http.*;
import org.springframework.util.MultiValueMap;
import java.util.*;
import java.nio.charset.StandardCharsets;
import com.google.common.hash.Hashing;
import org.apache.commons.validator.routines.UrlValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
@SpringBootApplication
@Controller
public class Application {
	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}
	@Autowired private StringRedisTemplate sharedData;
	@RequestMapping(value="/{id}", method = RequestMethod.GET)
	public void redirectTo(@PathVariable String id, HttpServletResponse resp) throws IOException {
		String key = sharedData.opsForValue().get(id);
		if (key != null) {
			resp.sendRedirect(key);
		} else {
			resp.sendError(HttpServletResponse.SC_NOT_FOUND);
		}
	}
	@RequestMapping(method = RequestMethod.POST)
	public ResponseEntity<String> shortener(@RequestParam MultiValueMap<String,String> form, HttpServletRequest req) throws IOException {
		String url = form.getFirst("url");
		UrlValidator urlValidator = new UrlValidator(new String[]{"http", "https"});
		if (urlValidator.isValid(url)) {
			String id = Hashing.murmur3_32().hashString(url, StandardCharsets.UTF_8).toString();
			sharedData.opsForValue().set(id, url);
			return new ResponseEntity<String>(req.getRequestURL().append(id+"\n").toString(), HttpStatus.CREATED);
		} else {
			return new ResponseEntity<String>(HttpStatus.BAD_REQUEST);
		}
	}
}
```
Ensure that your Redis instance is running. 
```
$ redis-server /usr/local/etc/redis.conf
```
