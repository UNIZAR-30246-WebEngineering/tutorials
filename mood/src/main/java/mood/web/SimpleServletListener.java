package mood.web;

import java.util.logging.Logger;

import javax.servlet.ServletContextAttributeEvent;
import javax.servlet.ServletContextAttributeListener;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;


/**
 * Web application lifecycle listener.
 *
 * @see <a href="https://docs.oracle.com/javaee/7/tutorial/servlets015.htm#GKCPG">The Java EE Tutorial: The mood Example Application</a>
 */
@WebListener
public class SimpleServletListener implements ServletContextListener,
        ServletContextAttributeListener {
    private static Logger log = Logger.getLogger("mood.web.SimpleServletListener");

    public void contextInitialized(ServletContextEvent sce) {
        log.info("Context initiallized");
    }

    public void contextDestroyed(ServletContextEvent sce) {
        log.info("Context destroyed");
    }

    public void attributeAdded(ServletContextAttributeEvent event) {
        log.info(
                "Attribute " + event.getName()
                        + " has been added, with value: " + event.getValue());
    }

    public void attributeRemoved(ServletContextAttributeEvent event) {
        log.info("Attribute " + event.getName() + " has been removed");
    }

    public void attributeReplaced(ServletContextAttributeEvent event) {
        log.info(
                "Attribute " + event.getName()
                        + " has been replaced, with value: " + event.getValue());
    }
}
