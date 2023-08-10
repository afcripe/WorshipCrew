package net.dahliasolutions.controllers.department;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.val;
import net.dahliasolutions.models.department.DepartmentCampus;
import net.dahliasolutions.models.department.DepartmentRegional;
import net.dahliasolutions.models.user.User;
import net.dahliasolutions.models.user.UserRoles;
import net.dahliasolutions.services.department.DepartmentCampusService;
import net.dahliasolutions.services.department.DepartmentRegionalService;
import net.dahliasolutions.services.RedirectService;
import net.dahliasolutions.services.user.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
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
        // allow only ADMIN and DIRECTOR
        if (!allowAccess(false)) {
            session.setAttribute("msgError", "Access Denied");
            return redirectService.pathName(session, "/");
        }
        // provide list based on permission
        model.addAttribute("departmentList", getDepartments());
        redirectService.setHistory(session, "/department");
        return "admin/department/listDepartments";
    }

    @GetMapping("/{id}")
    public String getDepartment(@PathVariable BigInteger id, Model model, HttpSession session) {
        // allow only ADMIN
        if (!allowAccess(true)) {
            session.setAttribute("msgError", "Access Denied");
            return redirectService.pathName(session, "/");
        }
        // get department
        DepartmentRegional departmentRegional = departmentRegionalService.findById(id).orElse(null);

        // allow only in assign department if DIRECTOR
        List<DepartmentRegional> departmentList = getDepartments();
        if (!departmentList.contains(departmentRegional)) {
            session.setAttribute("msgError", "Access Denied");
            return redirectService.pathName(session, "/");
        }

        model.addAttribute("department", departmentRegional);
        redirectService.setHistory(session, "/department/id");
        return "admin/department/department";
    }

    @GetMapping("/new")
    public String setDepartment(HttpSession session, Model model) {
        // allow only ADMIN
        if (!allowAccess(true)) {
            session.setAttribute("msgError", "Access Denied");
            return redirectService.pathName(session, "/");
        }
        DepartmentRegional departmentRegional = new DepartmentRegional();
        model.addAttribute("department", departmentRegional);
        model.addAttribute("directorList", userService.findAll());
        return "admin/department/departmentNew";
    }

    @PostMapping("/create")
    public String createDepartment(@ModelAttribute DepartmentRegional departmentRegionalModel, Model model, HttpSession session ) {
        // allow only ADMIN
        if (!allowAccess(true)) {
            session.setAttribute("msgError", "Access Denied");
            return redirectService.pathName(session, "/");
        }

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
    public String editDepartment(@PathVariable BigInteger id, Model model, HttpSession session) {
        // allow only ADMIN
        if (!allowAccess(true)) {
            session.setAttribute("msgError", "Access Denied");
            return redirectService.pathName(session, "/");
        }
        // get department
        DepartmentRegional departmentRegional = departmentRegionalService.findById(id).orElse(null);

        // allow only in assign department if DIRECTOR
        List<DepartmentRegional> departmentList = getDepartments();
        if (!departmentList.contains(departmentRegional)) {
            session.setAttribute("msgError", "Access Denied");
            return redirectService.pathName(session, "/");
        }

        if(departmentRegional == null) {
            return "redirect:/department";
        }
        model.addAttribute("department", departmentRegional);
        model.addAttribute("directorList", userService.findAll());
        return "admin/department/departmentEdit";
    }

    @PostMapping("/update")
    public String updateDepartment(@ModelAttribute DepartmentRegional departmentRegionalModel, Model model, HttpSession session) {
        // allow only ADMIN
        if (!allowAccess(true)) {
            session.setAttribute("msgError", "Access Denied");
            return redirectService.pathName(session, "/");
        }
        // get department
        Optional<DepartmentRegional> department = departmentRegionalService.findById(departmentRegionalModel.getId());

        Optional<DepartmentRegional> existingDepartment = departmentRegionalService.findByName(departmentRegionalModel.getName());
        if (department.isEmpty()) {
            session.setAttribute("msgError", "Department Not Found!");
            return "redirect:/department";
        }

        // allow only in assign department if DIRECTOR
        List<DepartmentRegional> departmentList = getDepartments();
        if (!departmentList.contains(department.get())) {
            session.setAttribute("msgError", "Access Denied");
            return redirectService.pathName(session, "/");
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
        // allow only ADMIN
        if (!allowAccess(true)) {
            session.setAttribute("msgError", "Access Denied");
            return redirectService.pathName(session, "/");
        }

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

    /*  Determine Permissions */
    private boolean allowAccess(Boolean adminOnly){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) auth.getPrincipal();
        Collection<UserRoles> roles = user.getUserRoles();
        // init the list
        List<DepartmentRegional> departmentRegionalList = new ArrayList<>();

        for (UserRoles role : roles){
            if (role.getName().equals("ADMIN_WRITE") || role.getName().equals("ADMIN_READ")) {
                departmentRegionalList = departmentRegionalService.findAll();
                return true;
            }
            if (!adminOnly && role.getName().equals("DIRECTOR_WRITE") || role.getName().equals("DIRECTOR_READ")) {
                departmentRegionalList.add(user.getDepartment().getRegionalDepartment());
                return true;
            }
        }
        return false;
    }
    private List<DepartmentRegional> getDepartments(){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) auth.getPrincipal();
        Collection<UserRoles> roles = user.getUserRoles();
        // init the list
        List<DepartmentRegional> departmentRegionalList = new ArrayList<>();

        for (UserRoles role : roles){
            if (role.getName().equals("ADMIN_WRITE") || role.getName().equals("ADMIN_READ")) {
                return departmentRegionalService.findAll();
            }
            if (role.getName().equals("DIRECTOR_WRITE") || role.getName().equals("DIRECTOR_READ")) {
                departmentRegionalList.add(user.getDepartment().getRegionalDepartment());
            }
        }
        return departmentRegionalList;
    }
}
