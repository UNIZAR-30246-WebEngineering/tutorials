package urlshortener

import com.google.common.hash.Hashing
import io.swagger.v3.oas.models.ExternalDocumentation
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import org.apache.commons.validator.routines.UrlValidator
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.net.URI
import java.nio.charset.StandardCharsets
import javax.servlet.http.HttpServletRequest


@SpringBootApplication
class App {
    @Bean
    fun urlshortenerAPI(): OpenAPI? {
        return OpenAPI()
            .info(Info().title("URL Shortener")
                    .description("Web engineering sample application")
                    .version("v2022.0.1")
            )
            .externalDocs(
                ExternalDocumentation()
                    .description("Create a URL Shortener step-by-step")
                    .url("https://github.com/UNIZAR-30246-WebEngineering/tutorials/blob/master/urlshortener/README.md")
            )
    }
}

fun main(args: Array<String>) {
    runApplication<App>(*args)
}

@RestController
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