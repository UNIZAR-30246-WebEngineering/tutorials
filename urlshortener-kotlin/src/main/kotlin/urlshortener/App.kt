package urlshortener

import com.google.common.hash.Hashing
import io.swagger.annotations.ApiImplicitParam
import io.swagger.annotations.ApiImplicitParams
import io.swagger.annotations.ApiOperation
import org.apache.commons.validator.routines.UrlValidator
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
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