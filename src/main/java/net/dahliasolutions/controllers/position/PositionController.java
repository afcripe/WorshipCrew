package net.dahliasolutions.controllers.position;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import net.dahliasolutions.models.department.DepartmentRegional;
import net.dahliasolutions.models.position.PermissionTemplate;
import net.dahliasolutions.models.position.Position;
import net.dahliasolutions.models.user.User;
import net.dahliasolutions.models.user.UserRoles;
import net.dahliasolutions.services.*;
import net.dahliasolutions.services.position.PermissionTemplateService;
import net.dahliasolutions.services.position.PositionService;
import net.dahliasolutions.services.user.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigInteger;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/position")
@RequiredArgsConstructor
public class PositionController {

    private final PositionService positionService;
    private final UserService userService;
    private final PermissionTemplateService permissionTemplateService;
    private final RedirectService redirectService;

    @ModelAttribute
    public void addAttributes(Model model) {
        model.addAttribute("moduleTitle", "Positions");
        model.addAttribute("moduleLink", "/position");
    }

    @GetMapping("")
    public String getPositions(Model model, HttpSession session) {
        List<Position> positionList = positionService.findAll();
        List<PermissionTemplate> templateList = permissionTemplateService.findAll();

        model.addAttribute("templateList", templateList);
        model.addAttribute("positions", positionList);

        redirectService.setHistory(session, "/position");
        return "position/listPositions";
    }

    @GetMapping("/position/{id}")
    public String getPosition(@PathVariable BigInteger id, Model model, HttpSession session) {
        Optional<Position> position = positionService.findById(id);

        if (position.isEmpty()) {
            session.setAttribute("msgError", "Position Not Found!");
            return redirectService.pathName(session,"position");
        }

        redirectService.setHistory(session, "/position/id");
        model.addAttribute("position", position);
        return "position/position";
    }

    @GetMapping("/new")
    public String setPosition(Model model) {
        Position position = new Position();
        model.addAttribute("position", position);
        return "position/positionNew";
    }

    @PostMapping("/create")
    public String createPosition(@ModelAttribute Position positionModel, Model model, HttpSession session ) {
        Optional<Position> position = positionService.findByName(positionModel.getName());
        if(position.isPresent() && !position.get().getId().equals(positionModel.getId())) {
            session.setAttribute("msgError", "Position Name Already Exists!");
            model.addAttribute("position", positionModel);
            return "position/positionNew";
        }

        positionService.updatePosition(new Position(
                null,positionModel.getLevel(), positionModel.getName(), positionModel.getDirectorId(), ""
        ));

        session.setAttribute("msgSuccess", "Position Successfully Added.");
        return "redirect:/position";
    }

    @GetMapping("/edit/{id}")
    public String editPosition(@PathVariable BigInteger id, Model model) {
        Optional<Position> position = positionService.findById(id);
        if(position.isEmpty()) {
            return "redirect:/position";
        }
        model.addAttribute("position", position.get());
        return "position/positionEdit";
    }

    @PostMapping("/update")
    public String updatePosition(@ModelAttribute Position positionModel, Model model, HttpSession session) {
        Optional<Position> position = positionService.findById(positionModel.getId());
        Optional<Position> existingPosition = positionService.findByName(positionModel.getName());
        if (position.isEmpty()) {
            session.setAttribute("msgError", "Position Not Found!");
            return "redirect:/position";
        }
        if (existingPosition.isPresent() && !existingPosition.get().getId().equals(position.get().getId())) {
            session.setAttribute("msgError", "Position Name Already Exists!");
            model.addAttribute("position", positionModel);
            String template = "/position/"+positionModel.getId().toString()+"/edit";
            return "redirect:"+template;
        }
        positionService.updatePosition(positionModel);
        session.setAttribute("msgSuccess", "Position Successfully Updated.");
        return "redirect:/position";
    }

    @GetMapping("/delete/{id}")
    public String deletePosition(@PathVariable BigInteger id, HttpSession session) {
        List<User> userList = userService.findAllByPosition(positionService.findById(id).orElse(null));
        if (userList.size() > 0){
            session.setAttribute("msgError", "Users are assigned to position!");
            return "redirect:/position";
        }
        positionService.deletePositionById(id);
        return "redirect:/position";
    }

    private boolean positionEdit(){
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Collection<UserRoles> roles = user.getUserRoles();

        for (UserRoles role : roles){
            if (role.getName().equals("ADMIN_WRITE")) {
                return true;
            }
        }

        return false;
    }
}
