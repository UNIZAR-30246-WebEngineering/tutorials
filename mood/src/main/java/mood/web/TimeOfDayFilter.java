package mood.web;

import java.io.IOException;
import java.util.Calendar;
import java.util.GregorianCalendar;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.annotation.WebInitParam;


/**`
 * @see <a href="https://docs.oracle.com/javaee/7/tutorial/servlets015.htm#GKCPG">The Java EE Tutorial: The mood Example Application</a>
 */
@WebFilter(filterName = "TimeOfDayFilter", urlPatterns = {"/*"}
       , initParams = {@WebInitParam(name = "mood", value = "awake")})
public class TimeOfDayFilter implements Filter {
    private String mood = null;

    public void init(FilterConfig filterConfig) throws ServletException {
        mood = filterConfig.getInitParameter("mood");
    }

    public void doFilter(
            ServletRequest req,
            ServletResponse res,
            FilterChain chain) throws IOException, ServletException {
        Calendar cal = GregorianCalendar.getInstance();

        switch (cal.get(Calendar.HOUR_OF_DAY)) {
            case 22:
            case 23:
            case 24:
            case 1:
            case 2:
            case 3:
            case 4:
            case 5:
            case 6:
            case 7:
                mood = "sleepy";

                break;

            case 8:
            case 13:
            case 18:
                mood = "hungry";

                break;

            case 9:
            case 10:
            case 11:
            case 12:
            case 14:
            case 16:
            case 17:
                mood = "alert";

                break;

            case 15:
                mood = "in need of coffee";

                break;

            case 19:
            case 20:
                mood = "content";

                break;

            case 21:
                mood = "lethargic";

                break;
        }

        req.setAttribute("mood", mood);
        chain.doFilter(req, res);
    }

    public void destroy() {
    }
}
