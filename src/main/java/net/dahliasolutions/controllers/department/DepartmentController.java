package net.dahliasolutions.controllers.department;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import net.dahliasolutions.models.department.DepartmentCampus;
import net.dahliasolutions.models.department.DepartmentRegional;
import net.dahliasolutions.models.user.User;
import net.dahliasolutions.services.department.DepartmentCampusService;
import net.dahliasolutions.services.department.DepartmentRegionalService;
import net.dahliasolutions.services.RedirectService;
import net.dahliasolutions.services.user.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/department")
@RequiredArgsConstructor
public class DepartmentController {

    private final DepartmentRegionalService departmentRegionalService;
    private final DepartmentCampusService departmentCampusService;
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
        List<DepartmentRegional> departmentRegionalList = departmentRegionalService.findAll();
        model.addAttribute("departmentList", departmentRegionalList);
        return "admin/department/listDepartments";
    }

    @GetMapping("/{id}")
    public String getDepartment(@PathVariable BigInteger id, Model model, HttpSession session) {
        redirectService.setHistory(session, "/department/id");
        DepartmentRegional departmentRegional = departmentRegionalService.findById(id).orElse(null);
        model.addAttribute("department", departmentRegional);
        return "admin/department/department";
    }

    @GetMapping("/new")
    public String setDepartment(Model model) {
        DepartmentRegional departmentRegional = new DepartmentRegional();
        model.addAttribute("department", departmentRegional);
        model.addAttribute("directorList", userService.findAll());
        return "admin/department/departmentNew";
    }

    @PostMapping("/create")
    public String createDepartment(@ModelAttribute DepartmentRegional departmentRegionalModel, Model model, HttpSession session ) {
        DepartmentRegional position = departmentRegionalService.findByName(departmentRegionalModel.getName()).orElse(null);
        if(position != null) {
            session.setAttribute("msgError", "Department Name Already Exists!");
            model.addAttribute("department", departmentRegionalModel);
            return "admin/department/departmentNew";
        }
        departmentRegionalService.createDepartment(departmentRegionalModel.getName());
        session.setAttribute("msgSuccess", "Department Successfully Added.");
        return "redirect:/department";
    }

    @GetMapping("/{id}/edit")
    public String editDepartment(@PathVariable BigInteger id, Model model) {
        DepartmentRegional departmentRegional = departmentRegionalService.findById(id).orElse(null);
        if(departmentRegional == null) {
            return "redirect:/department";
        }
        model.addAttribute("department", departmentRegional);
        model.addAttribute("directorList", userService.findAll());
        return "admin/department/departmentEdit";
    }

    @PostMapping("/update")
    public String updateDepartment(@ModelAttribute DepartmentRegional departmentRegionalModel, Model model, HttpSession session) {
        Optional<DepartmentRegional> department = departmentRegionalService.findById(departmentRegionalModel.getId());
        Optional<DepartmentRegional> existingDepartment = departmentRegionalService.findByName(departmentRegionalModel.getName());
        if (department.isEmpty()) {
            session.setAttribute("msgError", "Department Not Found!");
            return "redirect:/department";
        }
        if (existingDepartment.isPresent()) {
            if (!existingDepartment.get().getId().equals(department.get().getId())) {
                session.setAttribute("msgError", "Department Name Already Exists!");
                model.addAttribute("department", departmentRegionalModel);
                String template = "/department/" + departmentRegionalModel.getId().toString() + "/edit";
                return "redirect:" + template;
            }
        }

        department.get().setName(departmentRegionalModel.getName());
        department.get().setDirectorId(departmentRegionalModel.getDirectorId());
        departmentRegionalService.updateDepartment(department.get());
        session.setAttribute("msgSuccess", "Department Successfully Updated.");
        return "redirect:/department";
    }

    @GetMapping("/{id}/delete")
    public String deleteDepartment(@PathVariable BigInteger id, HttpSession session) {
        Optional<DepartmentRegional> regionalDepartment = departmentRegionalService.findById(id);
        if (regionalDepartment.isEmpty()) {
            session.setAttribute("msgError", "Department Not Founf!");
            return "redirect:/department";
        }

        List<DepartmentCampus> campusList =
                departmentCampusService.findDepartmentCampusesByRegionalDepartment(regionalDepartment.get());
        for (DepartmentCampus dep : campusList) {
            List<User> userList = userService.findAllByDepartment(departmentCampusService.findById(id).orElse(null));
            if (userList.size() > 0){
                session.setAttribute("msgError", "Users are assigned to department!");
                return "redirect:/department";
            }
        }

        departmentRegionalService.deleteDepartmentById(id);
        return "redirect:/department";
    }
}
