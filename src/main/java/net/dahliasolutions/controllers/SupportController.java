package net.dahliasolutions.controllers;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import net.dahliasolutions.models.StoreItem;
import net.dahliasolutions.services.RedirectService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

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

    @GetMapping("/search/{searchTerm}")
    public String searchArticle(@PathVariable String searchTerm, Model model, HttpSession session) {
        redirectService.setHistory(session, "/support/search/title/"+searchTerm);
        String searcher = URLDecoder.decode(searchTerm, StandardCharsets.UTF_8);
        // List<StoreItem> itemList = storeItemService.searchAll(searcher);

        // model.addAttribute("storeItems", itemList);
        return "support/index";
    }

}
