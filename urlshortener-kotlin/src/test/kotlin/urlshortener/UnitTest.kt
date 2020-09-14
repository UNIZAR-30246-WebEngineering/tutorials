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
}
