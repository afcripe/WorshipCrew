package net.dahliasolutions.controllers.campus;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import net.dahliasolutions.models.campus.Campus;
import net.dahliasolutions.models.campus.CampusModel;
import net.dahliasolutions.models.department.DepartmentCampus;
import net.dahliasolutions.models.department.DepartmentRegional;
import net.dahliasolutions.models.user.User;
import net.dahliasolutions.models.user.UserRoles;
import net.dahliasolutions.services.*;
import net.dahliasolutions.services.campus.CampusService;
import net.dahliasolutions.services.department.DepartmentCampusService;
import net.dahliasolutions.services.department.DepartmentRegionalService;
import net.dahliasolutions.services.user.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigInteger;
import java.util.*;

@Controller
@RequestMapping("/campus")
@RequiredArgsConstructor
public class CampusController {

    private final CampusService campusService;
    private final UserService userService;
    private final RedirectService redirectService;
    private final DepartmentCampusService departmentCampusService;
    private final DepartmentRegionalService departmentRegionalService;

    @ModelAttribute
    public void addAttributes(Model model) {
        model.addAttribute("moduleTitle", "Settings");
        model.addAttribute("moduleLink", "/admin");
    }

    @GetMapping("")
    public String getCampuses(Model model, HttpSession session) {
        // get persmissions
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) auth.getPrincipal();
        Collection<UserRoles> roles = currentUser.getUserRoles();
        boolean fullList = false;
        for (UserRoles role : roles){
            if (role.getName().equals("ADMIN_WRITE") || role.getName().equals("ADMIN_READ") ||
                    role.getName().equals("DIRECTOR_WRITE") || role.getName().equals("DIRECTOR_READ")) {
                fullList = true;
                break;
            }
        }

        if (!fullList) { return "redirect:/campus/"+currentUser.getCampus().getId(); }

        List<Campus> campusList = campusList = campusService.findAll();

        redirectService.setHistory(session, "/campus");
        model.addAttribute("campusList", campusList);
        return "admin/campus/listCampus";
    }

    @GetMapping("/showhidden")
    public String getCampusesIncludeHidden(Model model, HttpSession session) {
        redirectService.setHistory(session, "/campus/showhidden");
        List<Campus> campusList = campusService.findAllIncludeHidden();
        model.addAttribute("campusList", campusList);
        return "admin/campus/listCampus";
    }

    @GetMapping("/{id}")
    public String getCampus(@PathVariable BigInteger id, Model model, HttpSession session) {
        redirectService.setHistory(session, "/campus/"+id);

        Optional<Campus> campus = campusService.findById(id);
        if (campus.isEmpty()) {
            session.setAttribute("msgError", "Campus Not Found");
            return redirectService.pathName(session, "campus");
        }

        List<DepartmentCampus> departmentList = departmentCampusService.findAllByCampus(campus.get());


        model.addAttribute("campus", campus.get());
        model.addAttribute("departmentList", departmentList);
        return "admin/campus/campus";
    }

    @GetMapping("/new")
    public String setCampus(Model model) {
        CampusModel campusModel = new CampusModel(null, "", "", false, BigInteger.valueOf(0));
        model.addAttribute("campus", campusModel);
        model.addAttribute("directorList", userService.findAll());
        return "admin/campus/campusNew";
    }

    @PostMapping("/create")
    public String createCampus(@ModelAttribute CampusModel campusModel, Model model, HttpSession session ) {
        Campus location = campusService.findByName(campusModel.name()).orElse(null);
        if(location != null) {
            session.setAttribute("msgError", "Campus Name Already Exists!");
            model.addAttribute("campus", campusModel);
            return "admin/campus/campusNew";
        }

        campusService.createCampus(campusModel.name(), campusModel.city(), campusModel.directorId());
        session.setAttribute("msgSuccess", "Campus Successfully Added.");
        return redirectService.pathName(session, "campus");
    }

    @GetMapping("/{id}/edit")
    public String editCampus(@PathVariable BigInteger id, Model model, HttpSession session) {
        Campus campus = campusService.findById(id).orElse(null);
        if(campus == null) {
            return redirectService.pathName(session, "campus");
        }
        CampusModel campusModel = new CampusModel(
                campus.getId(), campus.getName(), campus.getCity(), campus.isHidden(), campus.getDirectorId());
        model.addAttribute("campus", campusModel);
        model.addAttribute("directorList", userService.findAll());
        model.addAttribute("mgrSelected", campus.getDirectorId());
        return "admin/campus/campusEdit";
    }

    @PostMapping("/update")
    public String updateCampus(@ModelAttribute CampusModel locationModel, Model model, HttpSession session) {
        Campus campus = campusService.findById(locationModel.id()).orElse(null);
        Campus existingCampus = campusService.findByName(locationModel.name()).orElse(null);
        if(campus == null) {
            session.setAttribute("msgError", "Campus Not Found!");
            return redirectService.pathName(session, "campus");
        }
        if(existingCampus != null && !Objects.equals(campus.getId(), existingCampus.getId())) {
            session.setAttribute("msgError", "Campus Name Already Exists! It may have been previously deleted and can be restored.");
            model.addAttribute("location", locationModel);
            model.addAttribute("directorList", userService.findAll());
            model.addAttribute("mgrSelected", locationModel.directorId());
            return redirectService.pathName(session, "campus");
        }

        campus.setName(locationModel.name());
        campus.setCity(locationModel.city());
        campus.setDirectorId(locationModel.directorId());
        campusService.save(campus);
        session.setAttribute("msgSuccess", "Campus Successfully Updated.");
        return redirectService.pathName(session, "campus");
    }

    @GetMapping("/{id}/delete")
    public String deleteLocation(@PathVariable BigInteger id, HttpSession session) {
        List<User> userList = userService.findAllByCampus(campusService.findById(id).orElse(null));
        if (userList.size() > 0){
            session.setAttribute("msgError", "Users are assigned to campus!");
            return redirectService.pathName(session, "campus");
        }
        campusService.deleteById(id);
        return redirectService.pathName(session, "campus");
    }

    @GetMapping("/{id}/restore")
    public String restoreLocation(@PathVariable BigInteger id, HttpSession session) {
        campusService.restoreById(id);
        return redirectService.pathName(session, "campus");
    }


    @GetMapping("/{cId}/department/{dId}")
    public String editDepartment(@PathVariable BigInteger cId, @PathVariable BigInteger dId, Model model, HttpSession session) {
        redirectService.setHistory(session, "/campus/"+cId);

        Optional<DepartmentCampus> departmentCampus = departmentCampusService.findById(dId);
        if(departmentCampus.isEmpty()) {
            session.setAttribute("msgError", "Department Not Found!");
            return "redirect:/campus/"+cId;
        }
        Optional<DepartmentRegional> regional = departmentRegionalService.findById(departmentCampus.get().getRegionalDepartment().getId());

        Optional<Campus> campus = campusService.findById(cId);
        List<User> userList = userService.findAllByCampus(campus.get());
        if (regional.get().getDirectorId() != null) {
            Optional<User> regionalDir = userService.findById(regional.get().getDirectorId());
            boolean userPresent = false;
            for (User u : userList) {
                if (u.getId().equals(regionalDir.get().getId())) {
                    userPresent = true;
                    break;
                }
            }
            if (!userPresent) {
                userList.add(regionalDir.get());
            }
        }


        model.addAttribute("department", departmentCampus.get());
        model.addAttribute("directorList", userList);
        return "admin/department/departmentCampusEdit";
    }

    @PostMapping("{id}/department/update")
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
}
