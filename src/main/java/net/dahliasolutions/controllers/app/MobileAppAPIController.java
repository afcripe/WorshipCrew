package net.dahliasolutions.controllers.app;

import lombok.RequiredArgsConstructor;
import net.dahliasolutions.models.support.Ticket;
import net.dahliasolutions.models.user.User;
import net.dahliasolutions.services.support.TicketService;
import net.dahliasolutions.services.user.UserService;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/app")
public class MobileAppAPIController {

    private final UserService userService;
    private final TicketService ticketService;

    @GetMapping("/login")
    public boolean getAppLogin() {
        return true;
    }

    @GetMapping("/tickets")
    public List<Ticket> getUserTickets() {
        User currentUser = userService.findByUsername("caleb@destinyworship.com").get();
        List<Ticket> openAgentTicketList = ticketService.findAllByAgentOpenOnly(currentUser);
        return openAgentTicketList;
    }

    @GetMapping("/listtickets")
    public String getUserTicketsHTML(Model model) {
        User currentUser = userService.findByUsername("caleb@destinyworship.com").get();
        List<Ticket> openAgentTicketList = ticketService.findAllByAgentOpenOnly(currentUser);
        model.addAttribute("agentTickets", openAgentTicketList);
        return "app/listTickets";
    }

}
