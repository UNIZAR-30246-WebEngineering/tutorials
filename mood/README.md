# The mood Example Application: a WebServlet application
The `mood` example application is a Spring Boot adaptation of the
`mood` example of the [Java EE 7 Tutorial](https://docs.oracle.com/javaee/7/tutorial/servlets015.htm#GKCPG). 
It is a simple example that displays Duke's moods at different times during the day.
## Components
The mood example application is comprised of three components: 

* `mood.web.MoodServlet`. This `HttpServlet` is the presentation layer of 
the application and. The servlet implements the `doGet` and `doPost` methods.
The `@WebServlet` annotation specifies the URL pattern:

    ```java
    @WebServlet("/report")
    public class MoodServlet extends HttpServlet {
        ...
        public void doGet(
            HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException {
        ...
    ```
* `mood.web.TimeOfDayFilter`. This `Filter` sets an initial vale to the attribute `mood`. 
The filter intercepts calls to the server and calls the `doFilter` that sets the value 
of the attribute `mood`.

    ```java
    @WebFilter(filterName = "TimeOfDayFilter", urlPatterns = {"/*"}
       , initParams = {@WebInitParam(name = "mood", value = "awake")})
    public class TimeOfDayFilter implements Filter {
        ...
        public void doFilter(
            ServletRequest req,
            ServletResponse res,
            FilterChain chain) throws IOException, ServletException {
        ...
    ```
* `mood.web.SmpleServletListener`. This `ServletContextListener` logs changes in
the lifecycle of the servlet.

The _context path_ of the application is specified in `application.properties`:

```properties
server.context-path=/mood
```

## Running the mood Example
The mood can be run from an IDE and from the command line with the support
of `gradle`. 
 
1. Make sure that `gradle` is available.
2. In a terminal window, go to `mood` folder.
3. Enter the following command to run the application:
    ```bash
    gradle bootRun
    ```
4. In a web browser, enter the following URL http://localhost:8080/mood/report
