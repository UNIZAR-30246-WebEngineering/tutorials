package ua;

import net.sf.uadetector.ReadableUserAgent;
import net.sf.uadetector.UserAgentStringParser;
import net.sf.uadetector.service.UADetectorServiceFactory;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = UserAgentTest.TestConfig.class)
public class UserAgentTest {

    private static final String CHROME =
            "Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.36 (KHTML, like Gecko)" +
            "Chrome/41.0.2228.0 Safari/537.36";

    @Autowired
    private WebApplicationContext wac;

    private MockMvc mockMvc;

    @SpringBootApplication
    @RestController
    static class TestConfig {

        @RequestMapping("/rest")
        @ResponseBody
        public String endpoint(@RequestHeader("User-Agent") String userAgent) {
            UserAgentStringParser parser = UADetectorServiceFactory.getResourceModuleParser();
            ReadableUserAgent rua = parser.parse(userAgent);
            return rua.getVersionNumber().getMajor();
        }

    }

    @Before
    public void setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
    }

    @Test
    public void extractVersionFromUserAgent() throws Exception {
        mockMvc.perform(get("/rest").header("User-Agent", CHROME)).
                andExpect(content().string("41"));
    }
}
