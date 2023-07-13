package net.dahliasolutions.controllers;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import net.dahliasolutions.services.RedirectService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/support")
@RequiredArgsConstructor
public class SupportController {

    private final RedirectService redirectService;

    @ModelAttribute
    public void addAttributes(Model model) {
        model.addAttribute("moduleTitle", "Support");
        model.addAttribute("moduleLink", "/support");
    }
    @GetMapping("")
    public String goSupportHome(Model model, HttpSession session) {
        redirectService.setHistory(session, "/support");
        return "support/index";
    }

}
