package controller.web;

import controller.model.Employee;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Locale;

@RestController
@RequestMapping("rest/employees")
public class EmployeeControllerRest {

    Employee employee = new Employee();

    @Autowired
    TemplateEngine templateEngine;

    @RequestMapping(value = "/{name}", method = RequestMethod.GET, produces = "application/json")
    public Employee getEmployeeInJSON(@PathVariable String name) {

        employee.setName(name);
        employee.setEmail("employee1@example.com");

        return employee;

    }

    @RequestMapping(value = "/{name}.xml", method = RequestMethod.GET, produces = "application/xml")
    public Employee getEmployeeInXML(@PathVariable String name) {

        employee.setName(name);
        employee.setEmail("employee1@example.com");

        return employee;

    }

    @RequestMapping(value = "/{name}.html", method = RequestMethod.GET, produces = "text/html")
    public String getEmployeeInHTML(@PathVariable String name) {

        employee.setName(name);
        employee.setEmail("employee1@example.com");

        Context ctx = new Context(Locale.getDefault());
        ctx.setVariable("employee", employee);

        return templateEngine.process("employee", ctx);
    }
}
