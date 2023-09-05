package net.dahliasolutions.controllers.app;

import lombok.RequiredArgsConstructor;
import net.dahliasolutions.models.UniversalAppSearchModel;
import net.dahliasolutions.models.order.OrderRequest;
import net.dahliasolutions.models.order.OrderStatus;
import net.dahliasolutions.models.support.*;
import net.dahliasolutions.models.user.User;
import net.dahliasolutions.models.user.UserRoles;
import net.dahliasolutions.services.order.OrderService;
import net.dahliasolutions.services.support.TicketService;
import net.dahliasolutions.services.user.UserService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigInteger;
import java.util.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/app")
public class MobileAppAPIController {

    private final UserService userService;
    private final TicketService ticketService;
    private final OrderService orderService;

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
                ticket.get().getAgent()

        );

        return t;
    }

    @GetMapping("/notelist/{id}")
    public List<AppTicketNote> getTicketNotesById(@PathVariable String id) {
        User currentUser = userService.findByUsername("caleb@destinyworship.com").get();
        Optional<Ticket> ticket = ticketService.findById(id);
        if (ticket.isEmpty()) {
            return new ArrayList<>();
        }

        ticket.get().setNotes(reverseNoteDateOrder(ticket.get().getNotes()));

        // determine if agent
        boolean isAgent = supportEditor(currentUser);
        if (currentUser.getId().equals(ticket.get().getUser().getId())) {
            // force agent and private false if ticket belongs to current user
            isAgent = false;
        }

        // filter notes if not agent
        List<AppTicketNote> noteList = new ArrayList<>();
        if (!isAgent) {
            for (TicketNote note : ticket.get().getNotes()) {
                if (!note.isNotePrivate()) {
                    noteList.add(new AppTicketNote(
                            note.getId(),
                            note.getNoteDate(),
                            note.isNotePrivate(),
                            note.isAgentNote(),
                            note.getUser().getFullName(),
                            note.getDetail(),
                            note.getImages()
                    ));
                }
            }
        } else {
            for (TicketNote note : ticket.get().getNotes()) {
                noteList.add(new AppTicketNote(
                        note.getId(),
                        note.getNoteDate(),
                        note.isNotePrivate(),
                        note.isAgentNote(),
                        note.getUser().getFullName(),
                        note.getDetail(),
                        note.getImages()
                ));
            }
        }

        return noteList;
    }

    @GetMapping("/agentlist/{id}")
    public List<User> getTicketAgentsById(@PathVariable String id) {
        User currentUser = userService.findByUsername("caleb@destinyworship.com").get();
        Optional<Ticket> ticket = ticketService.findById(id);
        if (ticket.isEmpty()) {
            return new ArrayList<>();
        }

        return ticket.get().getAgentList();
    }

    @GetMapping("/search/{searchTerm}")
    public List<UniversalAppSearchModel> getSearchResults(@PathVariable String searchTerm) {
        User currentUser = userService.findByUsername("caleb@destinyworship.com").get();

        if (searchTerm.equals("")) {
            return new ArrayList<>();
        }

        List<UniversalAppSearchModel> r = new ArrayList<>();

        // search for users
        List<User> users = userService.searchAllByFullName(searchTerm);
        for (User user : users) {
            r.add(new UniversalAppSearchModel("user", user.getId().toString(), user.getFullName(), ""));
        }

        // search for tickets
        List<Ticket> tickets = ticketService.searchAllById(searchTerm);
        for (Ticket ticket : tickets) {
            r.add(new UniversalAppSearchModel("ticket", ticket.getId(), ticket.getUser().getFullName(), ticket.getTicketDetail()));
        }


        boolean isNumber;
        try {
            Integer id = Integer.parseInt(searchTerm);
            isNumber = true;
        } catch (NumberFormatException nfe) {
            isNumber = false;
        }

        if (isNumber) {
            BigInteger orderId = new BigInteger(searchTerm);
            List<OrderRequest> requests = orderService.searchAllById(orderId);
            for (OrderRequest request : requests) {
                r.add(new UniversalAppSearchModel("request", request.getId().toString(), request.getUser().getFullName(), request.getRequestNote()));
            }
        }

        return r;
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
