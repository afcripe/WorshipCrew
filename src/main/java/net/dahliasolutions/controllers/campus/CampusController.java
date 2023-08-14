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
        model.addAttribute("moduleTitle", "Campus");
        model.addAttribute("moduleLink", "/campus");
    }

    @GetMapping("")
    public String getCampuses(Model model, HttpSession session) {
        // return list only when allowed else redirect to single campus
        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!campusesView()) {
            return "redirect:/campus/campus/"+currentUser.getCampus().getId();
        }

        List<Campus> campusList = campusList = campusService.findAll();

        model.addAttribute("campusEdit", campusEdit());
        model.addAttribute("departmentEdit", false);
        model.addAttribute("campusList", campusList);

        redirectService.setHistory(session, "/campus");
        return "campus/listCampus";
    }

    @GetMapping("/showhidden")
    public String getCampusesIncludeHidden(Model model, HttpSession session) {
        // only admins can view-edit hidden campuses
        if (!campusEdit()) {
            session.setAttribute("msgError", "Access Denied");
            return redirectService.pathName(session, "campus");
        }

        List<Campus> campusList = campusService.findAllIncludeHidden();

        model.addAttribute("campusEdit", campusEdit());
        model.addAttribute("departmentEdit", false);
        model.addAttribute("campusList", campusList);

        redirectService.setHistory(session, "/campus/showhidden");
        return "campus/listCampus";
    }

    @GetMapping("/campus/{id}")
    public String getCampus(@PathVariable BigInteger id, Model model, HttpSession session) {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Optional<Campus> campus = campusService.findById(id);
        if (campus.isEmpty()) {
            session.setAttribute("msgError", "Campus Not Found");
            return redirectService.pathName(session, "campus");
        }
        // verify permission
        if (!user.getCampus().getId().equals(id)) {
            if (!campusesView()) {
                session.setAttribute("msgError", "Access Denied");
                return redirectService.pathName(session, "campus");
            }
        }

        List<DepartmentCampus> departmentList = departmentCampusService.findAllByCampus(campus.get());

        model.addAttribute("user", user);
        model.addAttribute("campusEdit", campusEdit());
        model.addAttribute("departmentEdit", departmentEdit(campus.get()));
        model.addAttribute("campus", campus.get());
        model.addAttribute("departmentList", departmentList);

        redirectService.setHistory(session, "/campus/campus/"+id);
        return "campus/campus";
    }

    @GetMapping("/new")
    public String setCampus(Model model) {
        CampusModel campusModel = new CampusModel(null, "", "", false, BigInteger.valueOf(0));
        model.addAttribute("campus", campusModel);
        model.addAttribute("directorList", userService.findAll());
        return "campus/campusNew";
    }

    @PostMapping("/create")
    public String createCampus(@ModelAttribute CampusModel campusModel, Model model, HttpSession session ) {
        Campus location = campusService.findByName(campusModel.name()).orElse(null);
        if(location != null) {
            session.setAttribute("msgError", "Campus Name Already Exists!");
            model.addAttribute("campus", campusModel);
            return "campus/campusNew";
        }

        campusService.createCampus(campusModel.name(), campusModel.city(), campusModel.directorId());
        session.setAttribute("msgSuccess", "Campus Successfully Added.");
        return redirectService.pathName(session, "campus");
    }

    @GetMapping("/edit/{id}")
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
        return "campus/campusEdit";
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

    @GetMapping("/delete/{id}")
    public String deleteLocation(@PathVariable BigInteger id, HttpSession session) {
        List<User> userList = userService.findAllByCampus(campusService.findById(id).orElse(null));
        if (userList.size() > 0){
            session.setAttribute("msgError", "Users are assigned to campus!");
            return redirectService.pathName(session, "campus");
        }
        campusService.deleteById(id);
        return redirectService.pathName(session, "campus");
    }

    @GetMapping("/restore/{id}")
    public String restoreLocation(@PathVariable BigInteger id, HttpSession session) {
        campusService.restoreById(id);
        return redirectService.pathName(session, "campus");
    }


    @GetMapping("/campus/{cId}/department/{dId}")
    public String editDepartment(@PathVariable BigInteger cId, @PathVariable BigInteger dId, Model model, HttpSession session) {
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

        redirectService.setHistory(session, "/campus/campus/"+cId);
        return "campus/campusDepartmentEdit";
    }

    @PostMapping("/campus/{id}/department/update")
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

    private boolean campusesView() {
        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Collection<UserRoles> roles = currentUser.getUserRoles();
        for (UserRoles role : roles){
            if (role.getName().equals("ADMIN_WRITE")
                || role.getName().equals("DIRECTOR_WRITE")
                || role.getName().equals("DIRECTOR_READ")) {
                return true;
            }
        }
        return false;
    }

    private boolean campusEdit() {
        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Collection<UserRoles> roles = currentUser.getUserRoles();
        for (UserRoles role : roles){
            if (role.getName().equals("ADMIN_WRITE")) {
                return true;
            }
        }
        return false;
    }

    private boolean departmentEdit(Campus campus) {
        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Collection<UserRoles> roles = currentUser.getUserRoles();
        for (UserRoles role : roles){
            if (role.getName().equals("ADMIN_WRITE")) {
                return true;
            }
            if (role.getName().equals("CAMPUS_WRITE") && currentUser.getCampus().getId().equals(campus.getId())) {
                return true;
            }
        }
        return false;
    }
}
