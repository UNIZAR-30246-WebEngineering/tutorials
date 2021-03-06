# Differences between MVC and REST in Spring Framework


The goal of this example is showing the process of creating MVC endpoints and RESTful web services
in the Spring Framework. The difference is the way the HTTP response body is created. 
A traditional MVC controller (annotation  `@Controller`) relies on the underlying view 
technology (HTML) and object mapper technology (annotation `@ResponseBody` for other 
representation formats in Spring 3.x). A RESTful web service controller (annotation `@RestController` in 
Spring 4.x) relies only on the underlying object mapper technology which converts the returned object
into a specific representation format. If a RESTful web service controller wants to return HTML 
it may use the underlying template engine by autowiring a `TemplateEngine`.

This example contains two controllers with identical structure and purpose:
* `EmployeeControllerMvc` a MVC controller that returns JSON, XML and HTML. 
* `EmployeeControllerRest` a RESTful controller that returns JSON, XML and HTML.

The code for JSON and XML is nearly the same in both classes. They only differ in the 
use of the annotacion `@ResponseBody` in the methods MVC controller. 

The code for HTML is completely different. The MVC controller returns a `String` that
is the logical name of a view and the `Model` parameter contains the model to be merged with
the view. The RESTful controller returns as a `String` the HTML requested by the client. 

The code contains unit tests that shows that the requests to both controllers are identical.  

