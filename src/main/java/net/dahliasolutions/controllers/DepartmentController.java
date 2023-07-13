package net.dahliasolutions.controllers;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import net.dahliasolutions.models.Department;
import net.dahliasolutions.models.User;
import net.dahliasolutions.services.DepartmentService;
import net.dahliasolutions.services.RedirectService;
import net.dahliasolutions.services.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigInteger;
import java.util.List;

@Controller
@RequestMapping("/department")
@RequiredArgsConstructor
public class DepartmentController {

    private final DepartmentService departmentService;
    private final UserService userService;
    private final RedirectService redirectService;

    @ModelAttribute
    public void addAttributes(Model model) {
        model.addAttribute("moduleTitle", "Settings");
        model.addAttribute("moduleLink", "/admin");
    }

    @GetMapping("")
    public String getDepartments(Model model, HttpSession session) {
        redirectService.setHistory(session, "/department");
        List<Department> departmentList = departmentService.findAll();
        model.addAttribute("departmentList", departmentList);
        return "admin/listDepartments";
    }

    @GetMapping("/{id}")
    public String getDepartment(@PathVariable BigInteger id, Model model, HttpSession session) {
        redirectService.setHistory(session, "/department/id");
        Department department = departmentService.findById(id).orElse(null);
        model.addAttribute("department", department);
        return "admin/department";
    }

    @GetMapping("/new")
    public String setDepartment(Model model) {
        Department department = new Department();
        model.addAttribute("department", department);
        return "admin/departmentNew";
    }

    @PostMapping("/create")
    public String createDepartment(@ModelAttribute Department departmentModel, Model model, HttpSession session ) {
        Department position = departmentService.findByName(departmentModel.getName()).orElse(null);
        if(position != null) {
            session.setAttribute("msgError", "Department Name Already Exists!");
            model.addAttribute("department", departmentModel);
            return "admin/departmentNew";
        }
        departmentService.createDepartment(departmentModel.getName());
        session.setAttribute("msgSuccess", "Department Successfully Added.");
        return "redirect:/department";
    }

    @GetMapping("/{id}/edit")
    public String editDepartment(@PathVariable BigInteger id, Model model) {
        Department department = departmentService.findById(id).orElse(null);
        if(department == null) {
            return "redirect:/department";
        }
        model.addAttribute("department", department);
        return "admin/departmentEdit";
    }

    @PostMapping("/update")
    public String updateDepartment(@ModelAttribute Department departmentModel, Model model, HttpSession session) {
        Department department = departmentService.findById(departmentModel.getId()).orElse(null);
        Department existingDepartment = departmentService.findByName(departmentModel.getName()).orElse(null);
        if (department == null) {
            session.setAttribute("msgError", "Department Not Found!");
            return "redirect:/department";
        }
        if (existingDepartment != null) {
            session.setAttribute("msgError", "Department Name Already Exists!");
            model.addAttribute("department", departmentModel);
            String template = "/department/"+departmentModel.getId().toString()+"/edit";
            return "redirect:"+template;
        }
        departmentService.updateDepartment(departmentModel);
        session.setAttribute("msgSuccess", "Location Successfully Updated.");
        return "redirect:/department";
    }

    @GetMapping("/{id}/delete")
    public String deleteDepartment(@PathVariable BigInteger id, HttpSession session) {
        List<User> userList = userService.findAllByDepartment(departmentService.findById(id).orElse(null));
        if (userList.size() > 0){
            session.setAttribute("msgError", "Users are assigned to position!");
            return "redirect:/department";
        }
        departmentService.deleteDepartmentById(id);
        return "redirect:/department";
    }
}
