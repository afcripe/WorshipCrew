package net.dahliasolutions.controllers;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import net.dahliasolutions.models.Position;
import net.dahliasolutions.models.User;
import net.dahliasolutions.services.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigInteger;
import java.util.List;

@Controller
@RequestMapping("/position")
@RequiredArgsConstructor
public class PositionController {

    private final PositionService positionService;
    private final UserService userService;
    private final RedirectService redirectService;

    @GetMapping("")
    public String getPosition(Model model, HttpSession session) {
        redirectService.setHistory(session, "/position");
        List<Position> positionList = positionService.findAll();
        model.addAttribute("positions", positionList);
        return "admin/listPositions";
    }

    @GetMapping("/{id}")
    public String getPosition(@PathVariable BigInteger id, Model model, HttpSession session) {
        redirectService.setHistory(session, "/position/ id");
        Position position = positionService.findById(id).orElse(null);
        model.addAttribute("position", position);
        return "admin/position";
    }

    @GetMapping("/new")
    public String setPosition(Model model) {
        Position position = new Position();
        model.addAttribute("position", position);
        return "admin/positionNew";
    }

    @PostMapping("/create")
    public String createPosition(@ModelAttribute Position positionModel, Model model, HttpSession session ) {
        Position position = positionService.findByName(positionModel.getName()).orElse(null);
        if(position != null) {
            session.setAttribute("msgError", "Position Name Already Exists!");
            model.addAttribute("position", positionModel);
            return "admin/positionNew";
        }
        positionService.createPosition(positionModel.getName());
        session.setAttribute("msgSuccess", "Position Successfully Added.");
        return "redirect:/position";
    }

    @GetMapping("/{id}/edit")
    public String editPosition(@PathVariable BigInteger id, Model model) {
        Position position = positionService.findById(id).orElse(null);
        if(position == null) {
            return "redirect:/position";
        }
        model.addAttribute("position", position);
        return "admin/positionEdit";
    }

    @PostMapping("/update")
    public String updatePosition(@ModelAttribute Position positionModel, Model model, HttpSession session) {
        Position position = positionService.findById(positionModel.getId()).orElse(null);
        Position existingPosition = positionService.findByName(positionModel.getName()).orElse(null);
        if (position == null) {
            session.setAttribute("msgError", "Location Not Found!");
            return "redirect:/position";
        }
        if (existingPosition != null) {
            session.setAttribute("msgError", "Location Name Already Exists!");
            model.addAttribute("location", positionModel);
            String template = "/position/"+positionModel.getId().toString()+"/edit";
            return "redirect:"+template;
        }
        positionService.updatePosition(positionModel);
        session.setAttribute("msgSuccess", "Location Successfully Updated.");
        return "redirect:/position";
    }

    @GetMapping("/{id}/delete")
    public String deletePosition(@PathVariable BigInteger id, HttpSession session) {
        List<User> userList = userService.findAllByPosition(positionService.findById(id).orElse(null));
        if (userList.size() > 0){
            session.setAttribute("msgError", "Users are assigned to position!");
            return "redirect:/position";
        }
        positionService.deletePositionById(id);
        return "redirect:/position";
    }
}
