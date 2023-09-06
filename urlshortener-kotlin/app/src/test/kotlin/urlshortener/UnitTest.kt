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
}
