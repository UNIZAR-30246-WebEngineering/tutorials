package mood.web;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 * @see <a href="https://docs.oracle.com/javaee/7/tutorial/servlets015.htm#GKCPG">The Java EE Tutorial: The mood Example Application</a>
 */
@WebServlet("/report")
public class MoodServlet extends HttpServlet {
    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request  servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException      if an I/O error occurs
     */
    @Override
    public void doGet(
            HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        processRequest(request, response);
    }

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     *
     * @param request  servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException      if an I/O error occurs
     */
    private void processRequest(
            HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");

        try (PrintWriter out = response.getWriter()) {
            out.println("<html>");
            out.println("<head>");
            out.println("<title>Servlet MoodServlet</title>");
            out.println("</head>");
            out.println("<body>");
            out.println(
                    "<h1>Servlet MoodServlet at " + request.getContextPath()
                            + "</h1>");

            String mood = (String) request.getAttribute("mood");
            out.println("<p>Duke's mood is: " + mood + "</p>");

            switch (mood) {
                case "sleepy":
                    out.println("<img src=\"images/duke.snooze.gif\" /><br/>");
                    break;
                case "alert":
                    out.println("<img src=\"images/duke.waving.gif\" /><br/>");
                    break;
                case "hungry":
                    out.println("<img src=\"images/duke.cookies.gif\" /><br/>");
                    break;
                case "lethargic":
                    out.println("<img src=\"images/duke.handsOnHips.gif\" /><br/>");
                    break;
                case "content":
                    out.println("<img src=\"images/duke.pensive.gif\" /><br/>");
                    break;
                default:
                    out.println("<img src=\"images/duke.thumbsup.gif\" /><br/>");
                    break;
            }

            out.println("</body>");
            out.println("</html>");
        }
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request  servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException      if an I/O error occurs
     */
    @Override
    public void doPost(
            HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    } // </editor-fold>
}
