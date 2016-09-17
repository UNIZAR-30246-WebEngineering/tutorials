package urlshortener;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.net.URI;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.*;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment= RANDOM_PORT)
@DirtiesContext
public class IntegrationTests {

	private static final String HTTP_EXAMPLE_COM = "http://example.com/";
	private static final String HASH_HTTP_EXAMPLE_COM = "f684a3c4";

	@Value("${local.server.port}")
	private int port = 0;

	@Test
	public void testCreation() throws Exception {
		ResponseEntity<String> entity = createLink(HTTP_EXAMPLE_COM);
		assertThat(entity.getStatusCode(), is(HttpStatus.CREATED));
		assertThat(entity.getHeaders().getLocation(), is(URI.create("http://localhost:" + this.port + "/" + HASH_HTTP_EXAMPLE_COM)));
	}

	private ResponseEntity<String> createLink(String link) {
		MultiValueMap<String, Object> parts = new LinkedMultiValueMap<>();
		parts.add("url", link);
		return new TestRestTemplate().postForEntity("http://localhost:" + this.port, parts,
				String.class);
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
