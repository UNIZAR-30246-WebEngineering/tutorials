package urlshortener;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.net.URI;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
@WebAppConfiguration
@IntegrationTest("server.port=0")
@DirtiesContext
public class SystemTests {

	private static final String HTTP_EXAMPLE_COM = "http://example.com/";
	private static final String HASH_HTTP_EXAMPLE_COM = "f684a3c4";

	@Value("${local.server.port}")
	private int port = 0;

	@Test
	public void testCreation() throws Exception {
		ResponseEntity<String> entity = createLink(HTTP_EXAMPLE_COM);
		assertThat(entity.getStatusCode(), is(HttpStatus.CREATED));
		assertThat(entity.getBody(), is("http://localhost:" + this.port + "/" + HASH_HTTP_EXAMPLE_COM + "\n"));
	}

	private ResponseEntity<String> createLink(String link) {
		MultiValueMap<String, Object> parts = new LinkedMultiValueMap<>();
		parts.add("url", link);
		ResponseEntity<String> entity = new TestRestTemplate().postForEntity("http://localhost:" + this.port, parts,
				String.class);
		return entity;
	}

	@Test
	public void testRedirection() throws Exception {
		createLink(HTTP_EXAMPLE_COM);
		ResponseEntity<String> entity = new TestRestTemplate()
				.getForEntity("http://localhost:" + this.port + "/" + HASH_HTTP_EXAMPLE_COM, String.class);
		assertThat(entity.getStatusCode(), is(HttpStatus.FOUND));
		assertThat(entity.getHeaders().getLocation(), is(new URI(HTTP_EXAMPLE_COM)));
	}
}
