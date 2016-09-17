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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
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
			URI location = URI.create(req.getRequestURL().append(id).toString());
			HttpHeaders responseHeaders = new HttpHeaders();
			responseHeaders.setLocation(location);
			return new ResponseEntity<>(responseHeaders, HttpStatus.CREATED);
		} else {
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
	}
}
