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

	private static final String HTTP_EXAMPLE_COM = "http://example.com/";
	private static final String HASH = "f684a3c4";
	private static final String HASH_HTTP_EXAMPLE_COM = "http://localhost/"+HASH;

	@MockBean
	private ValueOperations<String, String> valueOperations;

	@MockBean
	private StringRedisTemplate stringRedisTemplate;

	@Autowired
	private MockMvc mvc;

	@Test
	public void testCreation() throws Exception {
		given(stringRedisTemplate.opsForValue()).willReturn(valueOperations);
		this.mvc.perform(post("/")
				.contentType(MediaType.APPLICATION_FORM_URLENCODED).param("url", HTTP_EXAMPLE_COM)).
				andExpect(status().isCreated()).
				andExpect(header().string("Location", is(HASH_HTTP_EXAMPLE_COM)));
	}


	@Test
	public void testRedirection() throws Exception {
		given(stringRedisTemplate.opsForValue()).willReturn(valueOperations);
		given(valueOperations.get(HASH)).willReturn(HTTP_EXAMPLE_COM);
		this.mvc.perform(get("/"+HASH)).
				andExpect(status().isTemporaryRedirect()).
				andExpect(header().string("Location", is(HTTP_EXAMPLE_COM)));
	}

}
