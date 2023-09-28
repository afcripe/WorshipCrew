package net.dahliasolutions.controllers;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import net.dahliasolutions.models.AdminSettings;
import net.dahliasolutions.models.wiki.WikiPost;
import net.dahliasolutions.services.AdminSettingsService;
import net.dahliasolutions.services.RedirectService;
import net.dahliasolutions.services.wiki.WikiPostService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/releasenotes")
@RequiredArgsConstructor
public class ReleaseNotesController {

    private final RedirectService redirectService;

    @ModelAttribute
    public void addAttributes(Model model) {
        model.addAttribute("moduleTitle", "DWC");
        model.addAttribute("moduleLink", "/releasenotes");
    }

    @GetMapping("")
    public String getReleaseNotes(Model model, HttpSession session) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        System.out.println(auth.getPrincipal().toString());
        if (auth.getPrincipal().toString().equals("anonymousUser")) {
            return "redirect:/";
        }

        redirectService.setHistory(session, "/releasenotes");
        return "releaseNotes";
    }
}
