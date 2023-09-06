package net.dahliasolutions.controllers.app;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import net.dahliasolutions.models.APIUser;
import net.dahliasolutions.models.UniversalAppSearchModel;
import net.dahliasolutions.models.order.OrderRequest;
import net.dahliasolutions.models.order.OrderStatus;
import net.dahliasolutions.models.support.*;
import net.dahliasolutions.models.user.User;
import net.dahliasolutions.models.user.UserRoles;
import net.dahliasolutions.services.JwtService;
import net.dahliasolutions.services.order.OrderService;
import net.dahliasolutions.services.support.TicketService;
import net.dahliasolutions.services.user.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigInteger;
import java.net.http.HttpRequest;
import java.util.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/app")
public class MobileAppAPIController {

    private final JwtService jwtService;
    private final UserService userService;
    private final TicketService ticketService;
    private final OrderService orderService;

    @GetMapping("/login")
    public boolean getAppLogin() {
        return true;
    }

    @GetMapping("/tickets")
    public ResponseEntity<List<Ticket>> getUserTickets(HttpServletRequest request) {
        APIUser apiUser = getUserFromToken(request);
        if (!apiUser.isValid()) {
            return new ResponseEntity<>(new ArrayList<>(), HttpStatus.FORBIDDEN);
        }

        List<Ticket> openAgentTicketList = ticketService.findAllByAgentOpenOnly(apiUser.getUser());
        return new ResponseEntity<>(openAgentTicketList, HttpStatus.OK);
    }

    @GetMapping("/ticket/{id}")
    public ResponseEntity<AppTicket> getTicketById(@PathVariable String id, HttpServletRequest request) {
        APIUser apiUser = getUserFromToken(request);
        if (!apiUser.isValid()) {
            return new ResponseEntity<>(new AppTicket(), HttpStatus.FORBIDDEN);
        }

        Optional<Ticket> ticket = ticketService.findById(id);
        if (ticket.isEmpty()) {
            return new ResponseEntity<>(new AppTicket(), HttpStatus.BAD_REQUEST);
        }

        String closeDate = "true";
        if (ticket.get().getTicketClosed() == null) { closeDate="false"; }

        ticket.get().setNotes(reverseNoteDateOrder(ticket.get().getNotes()));

        // determine if agent
        boolean isAgent = supportEditor(apiUser.getUser());
        if (apiUser.getUser().getId().equals(ticket.get().getUser().getId())) {
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

        return new ResponseEntity<>(t, HttpStatus.OK);
    }

    @GetMapping("/notelist/{id}")
    public ResponseEntity<List<AppTicketNote>> getTicketNotesById(@PathVariable String id, HttpServletRequest request) {
        APIUser apiUser = getUserFromToken(request);
        if (!apiUser.isValid()) {
            return new ResponseEntity<>(new ArrayList<>(), HttpStatus.FORBIDDEN);
        }

        Optional<Ticket> ticket = ticketService.findById(id);
        if (ticket.isEmpty()) {
            return new ResponseEntity<>(new ArrayList<>(), HttpStatus.BAD_REQUEST);
        }

        ticket.get().setNotes(reverseNoteDateOrder(ticket.get().getNotes()));

        // determine if agent
        boolean isAgent = supportEditor(apiUser.getUser());
        if (apiUser.getUser().getId().equals(ticket.get().getUser().getId())) {
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

        return new ResponseEntity<>(noteList, HttpStatus.OK);
    }

    @GetMapping("/agentlist/{id}")
    public ResponseEntity<List<User>> getTicketAgentsById(@PathVariable String id, HttpServletRequest request) {
        APIUser apiUser = getUserFromToken(request);
        if (!apiUser.isValid()) {
            return new ResponseEntity<>(new ArrayList<>(), HttpStatus.FORBIDDEN);
        }

        Optional<Ticket> ticket = ticketService.findById(id);
        if (ticket.isEmpty()) {
            return new ResponseEntity<>(new ArrayList<>(), HttpStatus.BAD_REQUEST);
        }

        return new ResponseEntity<>(ticket.get().getAgentList(), HttpStatus.OK);
    }

    @GetMapping("/search/{searchTerm}")
    public ResponseEntity<List<UniversalAppSearchModel>> getSearchResults(@PathVariable String searchTerm, HttpServletRequest request) {
        APIUser apiUser = getUserFromToken(request);
        if (!apiUser.isValid()) {
            return new ResponseEntity<>(new ArrayList<>(), HttpStatus.FORBIDDEN);
        }

        if (searchTerm.equals("")) {
            return new ResponseEntity<>(new ArrayList<>(), HttpStatus.BAD_REQUEST);
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
            for (OrderRequest req : requests) {
                r.add(new UniversalAppSearchModel("request", req.getId().toString(), req.getUser().getFullName(), req.getRequestNote()));
            }
        }

        return new ResponseEntity<>(r, HttpStatus.OK);
    }

    private APIUser getUserFromToken(HttpServletRequest request) {
        final String authHeader = request.getHeader("Authorization");
        final String token;
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);
            Optional<User> currentUser = userService.findByUsername(jwtService.extractUsername(token));
            if (currentUser.isPresent()) {
                if (jwtService.isTokenValid(token, currentUser.get())) {
                    return new APIUser(true, currentUser.get());
                }
            }
        }
        return new APIUser(false, new User());
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
