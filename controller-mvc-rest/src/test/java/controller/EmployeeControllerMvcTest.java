package controller;

import controller.model.Employee;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.test.context.junit4.SpringRunner;

import java.nio.charset.Charset;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment= SpringBootTest.WebEnvironment.RANDOM_PORT)
public class EmployeeControllerMvcTest {
    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    public void getEmployeeInJSON() {
        ResponseEntity<Employee> employee = this.restTemplate.getForEntity(
                "/mvc/employees/{name}", Employee.class, "Phil");

        assertThat(employee.getStatusCode(), is(HttpStatus.OK));
        assertThat(employee.getHeaders().getContentType(), is(MediaType.APPLICATION_JSON_UTF8));
        assertThat(employee.getBody().getName(), is("Phil"));
    }

    @Test
    public void getEmployeeInXML() {
        ResponseEntity<Employee> employee = this.restTemplate.getForEntity(
                "/mvc/employees/{name}.xml", Employee.class, "Phil");

        assertThat(employee.getStatusCode(), is(HttpStatus.OK));
        assertThat(employee.getHeaders().getContentType(), is(MediaType.APPLICATION_XML));
        assertThat(employee.getBody().getName(), is("Phil"));
    }

    @Test
    public void getEmployeeInHTML() {
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.ACCEPT, MediaType.TEXT_HTML_VALUE);
        MediaType TEXT_HTML_UTF8_CHARSET = new MediaType(MediaType.TEXT_HTML, Charset.forName("utf-8"));

        HttpEntity<String> requestHeaders = new HttpEntity<>(headers);

        ResponseEntity<String> employee = this.restTemplate.exchange(
                "/mvc/employees/{name}.html", HttpMethod.GET, requestHeaders, String.class, "Phil");

        assertThat(employee.getStatusCode(), is(HttpStatus.OK));
        assertThat(employee.getHeaders().getContentType(), is(TEXT_HTML_UTF8_CHARSET));
        assertThat(employee.getBody(), containsString("Phil"));
    }
}
