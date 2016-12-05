package controller.web;

import controller.model.Employee;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("mvc/employees")
public class EmployeeControllerMvc {

    Employee employee = new Employee();

    @RequestMapping(value = "/{name}", method = RequestMethod.GET, produces = "application/json")
    public @ResponseBody Employee getEmployeeInJSON(@PathVariable String name) {

        employee.setName(name);
        employee.setEmail("employee1@example.com");

        return employee;

    }

    @RequestMapping(value = "/{name}.xml", method = RequestMethod.GET, produces = "application/xml")
    public @ResponseBody Employee getEmployeeInXML(@PathVariable String name) {

        employee.setName(name);
        employee.setEmail("employee1@example.com");

        return employee;

    }

    @RequestMapping(value = "/{name}.html", method = RequestMethod.GET, produces = "text/html")
    public String getEmployeeInHTML(@PathVariable String name, Model model) {

        employee.setName(name);
        employee.setEmail("employee1@example.com");
        model.addAttribute("employee", employee);

        return "employee";
 }
}
