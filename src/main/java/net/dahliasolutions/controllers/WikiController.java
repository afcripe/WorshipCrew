package net.dahliasolutions.controllers;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import net.dahliasolutions.services.RedirectService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/wiki")
@RequiredArgsConstructor
public class WikiController {

    private final RedirectService redirectService;
    @GetMapping("")
    public String goWikiHome(Model model, HttpSession session) {
        redirectService.setHistory(session, "/wiki");
        return "wiki/index";
    }

}
