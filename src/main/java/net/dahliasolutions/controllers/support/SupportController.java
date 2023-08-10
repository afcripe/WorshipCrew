package net.dahliasolutions.controllers.support;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import net.dahliasolutions.models.support.Ticket;
import net.dahliasolutions.models.user.User;
import net.dahliasolutions.services.RedirectService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
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
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) auth.getPrincipal();

        List<Ticket> myTickets = new ArrayList<>();
        List<Ticket> agentTickets = new ArrayList<>();
        List<Ticket> supervisorTickets = new ArrayList<>();
        List<Ticket> campusTickets = new ArrayList<>();
        List<Ticket> departmentTickets = new ArrayList<>();


        model.addAttribute("myTickets", myTickets);
        model.addAttribute("agentTickets", agentTickets);
        model.addAttribute("supervisorTickets", supervisorTickets);
        model.addAttribute("campusTickets", campusTickets);
        model.addAttribute("departmentTickets", departmentTickets);
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
