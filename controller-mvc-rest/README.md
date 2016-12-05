# Differences between MVC and REST in Spring Framework

The goal of this example is showing the process of creating MVC endpoints and RESTful web services
in the Spring Framework. The difference is the way the HTTP response body is created. 
A traditional MVC controller (annotation  `@Controller`) relies on the underlying view 
technology, meanwhile a RESTful web service controller (annotation `@RestController`) 
simply returns the object which is converted into a specific representation format. However,
a MVC controller may return JSON or XML (annotation `@ResponseBody`) or a RESTful web service
may return HTML by using the underlying template engine (autowiring a `TemplateEngine`).

This example contains two controllers with identical structure and purpose:
* `EmployeeControllerMvc` a MVC controller that returns JSON, XML and HTML.
* `EmployeeControllerRest` a RESTful controller that returns JSON, XML and HTML.

The code for JSON and XML is nearly the same in both classes. They only differ in the 
use of the annotacion `@ResponseBody` in the methods MVC controller.

The code for HTML is completely different. The MVC controller returns a `String` that
is the logical name of a view and the `Model` parameter contains the model to be merged with
the view. The RESTful controller returns as a `String` the HTML requested by the client. 

The code contains unit tests that shows that the requests to both controllers are identical.  