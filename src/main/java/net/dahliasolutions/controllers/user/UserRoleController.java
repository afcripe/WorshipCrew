package net.dahliasolutions.controllers.user;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import net.dahliasolutions.models.user.UserRoles;
import net.dahliasolutions.services.RedirectService;
import net.dahliasolutions.services.user.UserRolesService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/roles")
@RequiredArgsConstructor
public class UserRoleController {

    private final UserRolesService rolesService;
    private final RedirectService redirectService;

    @ModelAttribute
    public void addAttributes(Model model) {
        model.addAttribute("moduleTitle", "Settings");
        model.addAttribute("moduleLink", "/admin");
    }

    @GetMapping("")
    public String getLocations(Model model, HttpSession session) {
        redirectService.setHistory(session, "/roles");
        List<UserRoles> roleList = rolesService.findAll();
        model.addAttribute("roleList", roleList);
        return "user/listUserRoles";
    }
}
