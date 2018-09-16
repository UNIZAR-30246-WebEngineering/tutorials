package urlshortener;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = RANDOM_PORT)
public class IntegrationTest {

    private static final String HTTP_EXAMPLE_COM = "http://example.com/";
    private static final String HASH_HTTP_EXAMPLE_COM = "f684a3c4";
    private static final UriComponentsBuilder LOCATION = UriComponentsBuilder.fromUriString("http://localhost:{port}/{hash}");

    @Autowired
    private TestRestTemplate restTemplate;

    @LocalServerPort
    private int port;

    @Test
    public void testCreation() {
        MultiValueMap<String, Object> parts = new LinkedMultiValueMap<>();
        parts.add("url", HTTP_EXAMPLE_COM);
        ResponseEntity<String> response = restTemplate.postForEntity("/", parts,
                String.class);
        assertThat(response.getStatusCode(), is(HttpStatus.CREATED));

        Map<String, Object> components = new HashMap<>();
        components.put("port", port);
        components.put("hash", HASH_HTTP_EXAMPLE_COM);
        assertThat(response.getHeaders().getLocation(), is(LOCATION.build().expand(components).toUri()));
    }

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
}
