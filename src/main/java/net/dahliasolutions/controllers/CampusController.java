package net.dahliasolutions.controllers;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import net.dahliasolutions.models.*;
import net.dahliasolutions.services.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigInteger;
import java.util.List;
import java.util.Objects;

@Controller
@RequestMapping("/campus")
@RequiredArgsConstructor
public class CampusController {

    private final CampusService campusService;
    private final UserService userService;
    private final RedirectService redirectService;

    @ModelAttribute
    public void addAttributes(Model model) {
        model.addAttribute("moduleTitle", "Settings");
        model.addAttribute("moduleLink", "/admin");
    }

    @GetMapping("")
    public String getCampuses(Model model, HttpSession session) {
        redirectService.setHistory(session, "/campus");
        List<Campus> campusList = campusService.findAll();
        model.addAttribute("campusList", campusList);
        return "admin/listCampus";
    }

    @GetMapping("/showhidden")
    public String getCampusesIncludeHidden(Model model, HttpSession session) {
        redirectService.setHistory(session, "/campus/showhidden");
        List<Campus> campusList = campusService.findAllIncludeHidden();
        model.addAttribute("campusList", campusList);
        return "admin/listCampus";
    }

    @GetMapping("/{id}")
    public String getCampus(@PathVariable BigInteger id, Model model, HttpSession session) {
        redirectService.setHistory(session, "/campus/"+id);
        Campus location = campusService.findById(id).orElse(null);
        model.addAttribute("campus", location);
        return "admin/campus";
    }

    @GetMapping("/new")
    public String setCampus(Model model) {
        CampusModel campusModel = new CampusModel(null, "", "", false, BigInteger.valueOf(0));
        model.addAttribute("campus", campusModel);
        model.addAttribute("managerList", userService.findAll());
        return "admin/campusNew";
    }

    @PostMapping("/create")
    public String createCampus(@ModelAttribute CampusModel campusModel, Model model, HttpSession session ) {
        Campus location = campusService.findByName(campusModel.name()).orElse(null);
        if(location != null) {
            session.setAttribute("msgError", "Campus Name Already Exists!");
            model.addAttribute("campus", campusModel);
            return "admin/campusNew";
        }

        campusService.createCampus(campusModel.name(), campusModel.city(), campusModel.managerId());
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
                campus.getId(), campus.getName(), campus.getCity(), campus.isHidden(), campus.getManagerId());
        model.addAttribute("campus", campusModel);
        model.addAttribute("managerList", userService.findAll());
        model.addAttribute("mgrSelected", campus.getManagerId());
        return "admin/campusEdit";
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
            model.addAttribute("managerList", userService.findAll());
            model.addAttribute("mgrSelected", locationModel.managerId());
            return redirectService.pathName(session, "campus");
        }

        campus.setName(locationModel.name());
        campus.setCity(locationModel.city());
        campus.setManagerId(locationModel.managerId());
        campusService.save(campus);
        session.setAttribute("msgSuccess", "Campus Successfully Updated.");
        return redirectService.pathName(session, "campus");
    }

    @GetMapping("/{id}/delete")
    public String deleteLocation(@PathVariable BigInteger id, HttpSession session) {
        List<User> userList = userService.findAllByLocation(campusService.findById(id).orElse(null));
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
}
