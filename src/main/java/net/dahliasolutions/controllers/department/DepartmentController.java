package net.dahliasolutions.controllers.department;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import net.dahliasolutions.models.campus.Campus;
import net.dahliasolutions.models.department.DepartmentCampus;
import net.dahliasolutions.models.department.DepartmentRegional;
import net.dahliasolutions.models.user.User;
import net.dahliasolutions.models.user.UserRoles;
import net.dahliasolutions.services.campus.CampusService;
import net.dahliasolutions.services.department.DepartmentCampusService;
import net.dahliasolutions.services.department.DepartmentRegionalService;
import net.dahliasolutions.services.RedirectService;
import net.dahliasolutions.services.user.UserService;
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
    private final CampusService campusService;
    private final RedirectService redirectService;

    @ModelAttribute
    public void addAttributes(Model model) {
        model.addAttribute("moduleTitle", "Departments");
        model.addAttribute("moduleLink", "/department");
    }

    @GetMapping("")
    public String getDepartments(Model model, HttpSession session) {
        // provide list based on permission
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Collection<UserRoles> roles = user.getUserRoles();
        // init the list

        for (UserRoles role : roles){
            // if admin, continue to listing
            if (role.getName().equals("ADMIN_WRITE")) {
                model.addAttribute("departmentList", departmentRegionalService.findAll());

                redirectService.setHistory(session, "/department");
                return "department/listDepartments";
            }
            // if director permissions redirect to regional department
            if (role.getName().equals("DIRECTOR_WRITE") || role.getName().equals("DIRECTOR_READ")) {
                return "redirect:/department/department/" + user.getDepartment().getRegionalDepartment().getId();
            }
        }
        session.setAttribute("msgError", "Problem loading Department");
        return redirectService.pathName(session, "/");
    }

    @GetMapping("/department/{id}")
    public String getDepartment(@PathVariable BigInteger id, Model model, HttpSession session) {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Collection<UserRoles> roles = user.getUserRoles();
        boolean isAdmin = false;
        for (UserRoles role : roles) {
            if (role.getName().equals("ADMIN_WRITE")) {
                isAdmin = true;
            }
        }
        // get department
        Optional<DepartmentRegional> departmentRegional = departmentRegionalService.findById(id);

        if (!departmentRegional.isPresent()) {
            if (departmentView(departmentRegional.get())){
                session.setAttribute("msgError", "Access Denied");
                return redirectService.pathName(session, "/");
            }
        }

        List<DepartmentCampus> departmentList = departmentCampusService.findDepartmentCampusesByRegionalDepartment(departmentRegional.get());

        model.addAttribute("isAdmin", isAdmin);
        model.addAttribute("department", departmentRegional.get());
        model.addAttribute("departmentList", departmentList);
        model.addAttribute("departmentEdit", departmentEdit(departmentRegional.get()));

        redirectService.setHistory(session, "/department/department/"+id);
        return "department/department";
    }

    @GetMapping("/new")
    public String setDepartment(HttpSession session, Model model) {
        // allow only ADMIN

        DepartmentRegional departmentRegional = new DepartmentRegional();
        model.addAttribute("department", departmentRegional);
        model.addAttribute("directorList", userService.findAll());
        return "department/departmentNew";
    }

    @PostMapping("/create")
    public String createDepartment(@ModelAttribute DepartmentRegional departmentRegionalModel, Model model, HttpSession session ) {
        // allow only ADMIN


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

    @GetMapping("/edit/{id}")
    public String editDepartment(@PathVariable BigInteger id, Model model, HttpSession session) {
        // get department
        DepartmentRegional departmentRegional = departmentRegionalService.findById(id).orElse(null);

        // allow only assigned director and admins
        List<DepartmentRegional> departmentList = getDepartmentsList();
        if (!departmentList.contains(departmentRegional)) {
            session.setAttribute("msgError", "Access Denied");
            return redirectService.pathName(session, "/");
        }

        if(departmentRegional == null) {
            return "redirect:/department";
        }
        model.addAttribute("department", departmentRegional);
        model.addAttribute("directorList", userService.findAll());
        return "department/departmentEdit";
    }

    @PostMapping("/update")
    public String updateDepartment(@ModelAttribute DepartmentRegional departmentRegionalModel, Model model, HttpSession session) {
        // get department
        Optional<DepartmentRegional> department = departmentRegionalService.findById(departmentRegionalModel.getId());

        Optional<DepartmentRegional> existingDepartment = departmentRegionalService.findByName(departmentRegionalModel.getName());
        if (department.isEmpty()) {
            session.setAttribute("msgError", "Department Not Found!");
            return "redirect:/department";
        }

        // allow only assigned director and admins
        List<DepartmentRegional> departmentList = getDepartmentsList();
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

    @GetMapping("/delete/{id}")
    public String deleteDepartment(@PathVariable BigInteger id, HttpSession session) {
        Optional<DepartmentRegional> regionalDepartment = departmentRegionalService.findById(id);
        if (regionalDepartment.isEmpty()) {
            session.setAttribute("msgError", "Department Not Founf!");
            return "redirect:/department";
        }

        departmentRegionalService.deleteDepartmentById(id);
        return "redirect:/department";
    }


    @GetMapping("/department/{dId}/campus/{cId}")
    public String editDepartment(@PathVariable BigInteger dId, @PathVariable BigInteger cId, Model model, HttpSession session) {
        Optional<DepartmentCampus> departmentCampus = departmentCampusService.findById(dId);
        if(departmentCampus.isEmpty()) {
            session.setAttribute("msgError", "Department Not Found!");
            return "redirect:/campus/campus/"+cId;
        }

        Optional<Campus> campus = campusService.findById(cId);
        List<User> userList = userService.findAllByCampus(campus.get());
        Optional<User> user = userService.findById(departmentCampus.get().getRegionalDepartment().getDirectorId());
        if (user.isPresent()) {
            if (!userList.contains(user.get())) {
                userList.add(user.get());
            }
        }

        model.addAttribute("department", departmentCampus.get());
        model.addAttribute("directorList", userList);

        redirectService.setHistory(session, "/department/department/"+dId);
        return "campus/campusDepartmentEdit";
    }

    @PostMapping("/department/{id}/campus/update")
    public String updateDepartment(@PathVariable BigInteger id, @ModelAttribute DepartmentCampus departmentCampusModel, Model model, HttpSession session) {
        Optional<DepartmentCampus> department = departmentCampusService.findById(departmentCampusModel.getId());
        if (department.isEmpty()) {
            session.setAttribute("msgError", "Department Not Found!");
            return redirectService.pathName(session, "campus");
        }

        department.get().setDirectorId(departmentCampusModel.getDirectorId());
        departmentCampusService.updateDepartment(department.get());

        session.setAttribute("msgSuccess", "Department Successfully Updated.");
        return redirectService.pathName(session, "campus");
    }


    /*  Determine Permissions */
    private boolean departmentView(DepartmentRegional department){
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Collection<UserRoles> roles = user.getUserRoles();

        for (UserRoles role : roles){
            if (role.getName().equals("ADMIN_WRITE")) {
                return true;
            }
            if (role.getName().equals("DIRECTOR_WRITE") || role.getName().equals("DIRECTOR_READ")) {
                if(user.getDepartment().getRegionalDepartment().equals(department)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean departmentEdit(DepartmentRegional department){
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Collection<UserRoles> roles = user.getUserRoles();

        for (UserRoles role : roles){
            if (role.getName().equals("ADMIN_WRITE")) {
                return true;
            }
            if (role.getName().equals("DIRECTOR_WRITE")) {
                if(user.getDepartment().getRegionalDepartment().equals(department)) {
                    return true;
                }
            }
        }

        return false;
    }

    private List<DepartmentRegional> getDepartmentsList(){
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Collection<UserRoles> roles = user.getUserRoles();
        // init the list
        List<DepartmentRegional> departmentRegionalList = new ArrayList<>();

        for (UserRoles role : roles){
            if (role.getName().equals("ADMIN_WRITE")) {
                return departmentRegionalService.findAll();
            }
            if (role.getName().equals("DIRECTOR_WRITE") || role.getName().equals("DIRECTOR_READ")) {
                departmentRegionalList.add(user.getDepartment().getRegionalDepartment());
            }
        }
        return departmentRegionalList;
    }
}
