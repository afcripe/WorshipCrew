package net.dahliasolutions.controllers.app;

import lombok.RequiredArgsConstructor;
import net.dahliasolutions.models.support.AppTicket;
import net.dahliasolutions.models.support.Ticket;
import net.dahliasolutions.models.support.TicketNote;
import net.dahliasolutions.models.user.User;
import net.dahliasolutions.models.user.UserRoles;
import net.dahliasolutions.services.support.TicketService;
import net.dahliasolutions.services.user.UserService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

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

    @GetMapping("/ticket/{id}")
    public AppTicket getTicketById(@PathVariable String id) {
        User currentUser = userService.findByUsername("caleb@destinyworship.com").get();
        Optional<Ticket> ticket = ticketService.findById(id);
        if (ticket.isEmpty()) {
            return new AppTicket();
        }

        String closeDate = "true";
        if (ticket.get().getTicketClosed() == null) { closeDate="false"; }

        ticket.get().setNotes(reverseNoteDateOrder(ticket.get().getNotes()));

        // determine if agent
        boolean isAgent = supportEditor(currentUser);
        if (currentUser.getId().equals(ticket.get().getUser().getId())) {
            // force agent and private false if ticket belongs to current user
            isAgent = false;
        }

        // filter notes if not agent
        List<TicketNote> noteList = new ArrayList<>();
        if (!isAgent) {
            for (TicketNote note : ticket.get().getNotes()) {
                if (!note.isNotePrivate()) {
                    noteList.add(note);
                }
            }
        } else {
            noteList = ticket.get().getNotes();
        }

        // create the App Ticket
        AppTicket t = new AppTicket(
                ticket.get().getId(),
                ticket.get().getTicketDate(),
                ticket.get().getTicketDue(),
                ticket.get().getTicketClosed(),
                ticket.get().getTicketDetail(),
                ticket.get().getPriority(),
                isAgent,
                ticket.get().getSla(),
                ticket.get().getTicketStatus(),
                ticket.get().getCampus(),
                ticket.get().getDepartment(),
                ticket.get().getUser(),
                ticket.get().getAgent(),
                noteList,
                ticket.get().getAgentList()

        );

        return t;
    }

    private boolean supportEditor(User currentUser) {
        Collection<UserRoles> roles = currentUser.getUserRoles();
        for (UserRoles role : roles) {
            if (role.getName().equals("ADMIN_WRITE") || role.getName().equals("SUPPORT_AGENT")
                    || role.getName().equals("SUPPORT_SUPERVISOR")) {
                return true;
            }
        }
        return false;
    }
    private List<TicketNote> reverseNoteDateOrder(List<TicketNote> notes) {
        Collections.sort(notes,new Comparator<TicketNote>() {
            @Override
            public int compare (TicketNote note1, TicketNote note2){
                return note1.getNoteDate().compareTo(note2.getNoteDate());
            }
        });
        Collections.reverse(notes);
        return notes;
    }

}
