package xml;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.util.FileCopyUtils;
import org.w3c.dom.Document;
import org.xml.sax.SAXParseException;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasXPath;
import static org.mockito.Matchers.contains;

public class XmlDangersTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void billionLaughsAttack() throws Exception {
        Resource attack = new ClassPathResource("billion-laughs-attack.xml");
        String content = reader(attack);

        thrown.expect(SAXParseException.class);
        thrown.expectMessage(contains("\"JAXP00010001\""));
        getDocument(content);
    }

    @Test
    public void quadraticBlowup() throws Exception {
        Resource attack = new ClassPathResource("quadratic-blowup.xml");
        String content = reader(attack);

        thrown.expect(SAXParseException.class);
        thrown.expectMessage(contains("\"JAXP00010004\""));
        getDocument(content);
    }

    @Test
    public void xxe() throws Exception {
        Resource attack = new ClassPathResource("xxe.xml");
        String content = reader(attack);

        Document document = getDocument(content);

        assertThat(document, hasXPath("/post/content", containsString("root")));
    }

    private Document getDocument(String content) throws Exception {
        DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
        javax.xml.parsers.DocumentBuilder builder = docBuilderFactory.newDocumentBuilder();
        return builder.parse(new ByteArrayInputStream(content.getBytes()));
    }

    private String reader(Resource resource) {
        try (InputStream is = resource.getInputStream()) {
            return new String(FileCopyUtils.copyToByteArray(is));
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
    }
}
