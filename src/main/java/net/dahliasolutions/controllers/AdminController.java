package net.dahliasolutions.controllers;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import net.dahliasolutions.services.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminSettingsService adminSettingsService;
    private final RedirectService redirectService;

    @ModelAttribute
    public void addAttributes(Model model) {
        model.addAttribute("moduleTitle", "Settings");
        model.addAttribute("moduleLink", "/admin");
    }

    @GetMapping("")
    public String goAdminHome(Model model, HttpSession session) {
        redirectService.setHistory(session, "/admin");
        model.addAttribute("adminSettings", adminSettingsService.getAdminSettings());
        return "admin/index";
    }

}
