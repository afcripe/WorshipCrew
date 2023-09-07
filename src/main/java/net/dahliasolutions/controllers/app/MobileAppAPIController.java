package net.dahliasolutions.controllers.app;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import net.dahliasolutions.controllers.AuthenticationResponse;
import net.dahliasolutions.models.APIUser;
import net.dahliasolutions.models.AppItem;
import net.dahliasolutions.models.LoginModel;
import net.dahliasolutions.models.UniversalAppSearchModel;
import net.dahliasolutions.models.order.OrderItem;
import net.dahliasolutions.models.order.OrderRequest;
import net.dahliasolutions.models.order.OrderStatus;
import net.dahliasolutions.models.support.*;
import net.dahliasolutions.models.user.User;
import net.dahliasolutions.models.user.UserRoles;
import net.dahliasolutions.services.AuthService;
import net.dahliasolutions.services.JwtService;
import net.dahliasolutions.services.order.OrderItemService;
import net.dahliasolutions.services.order.OrderService;
import net.dahliasolutions.services.support.TicketService;
import net.dahliasolutions.services.user.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.bind.annotation.*;

import java.math.BigInteger;
import java.net.http.HttpRequest;
import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/app")
public class MobileAppAPIController {

    private final AuthService authService;
    private final JwtService jwtService;
    private final UserService userService;
    private final TicketService ticketService;
    private final OrderService orderService;
    private final OrderItemService orderItemService;

    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponse> getAuthUser(@ModelAttribute LoginModel loginModel) {
        AuthenticationResponse response = authService.authenticate(loginModel);
        if (response.isLoggedIn()) {
            return new ResponseEntity<>(response, HttpStatus.OK);
        }
        return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
    }

    @GetMapping("/renewtoken")
    public ResponseEntity<AuthenticationResponse> getAuthUser(HttpServletRequest request) {
        APIUser apiUser = getUserFromToken(request);
        if (!apiUser.isValid()) {
            return new ResponseEntity<>(new AuthenticationResponse(), HttpStatus.FORBIDDEN);
        }

        LoginModel loginModel = new LoginModel(apiUser.getUser().getUsername(), apiUser.getUser().getPassword());

        AuthenticationResponse response = authService.renewAuth(loginModel);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/ticketsbyuser")
    public ResponseEntity<List<AppItem>> getTicketsByUser(HttpServletRequest request) {
        APIUser apiUser = getUserFromToken(request);
        if (!apiUser.isValid()) {
            return new ResponseEntity<>(new ArrayList<>(), HttpStatus.FORBIDDEN);
        }

        List<AppItem> appItemList = new ArrayList<>();
        List<Ticket> openAgentTicketList = ticketService.findAllByUser(apiUser.getUser());

        for (Ticket tkt : openAgentTicketList) {
            if (!tkt.getTicketStatus().equals(TicketStatus.Closed)) {
                appItemList.add(new AppItem(
                        tkt.getId(),
                        tkt.getTicketStatus().toString(),
                        tkt.getTicketDetail(),
                        tkt.getTicketDate(),
                        0,
                        tkt.getUser().getFullName(),
                        "tickets"));
            }
        }

        return new ResponseEntity<>(appItemList, HttpStatus.OK);
    }

    @GetMapping("/tickets")
    public ResponseEntity<List<AppItem>> getUserTickets(HttpServletRequest request) {
        APIUser apiUser = getUserFromToken(request);
        if (!apiUser.isValid()) {
            return new ResponseEntity<>(new ArrayList<>(), HttpStatus.FORBIDDEN);
        }

        List<AppItem> appItemList = new ArrayList<>();
        List<Ticket> openAgentTicketList = ticketService.findAllByAgentOpenOnly(apiUser.getUser());

        for (Ticket tkt : openAgentTicketList) {
            appItemList.add(new AppItem(
                    tkt.getId(),
                    tkt.getTicketStatus().toString(),
                    tkt.getTicketDetail(),
                    tkt.getTicketDate(),
                    0,
                    tkt.getUser().getFullName(),
                    "tickets"));
        }

        return new ResponseEntity<>(appItemList, HttpStatus.OK);
    }

    @GetMapping("/ticketincludes")
    public ResponseEntity<List<AppItem>> getUserTicketMentions(HttpServletRequest request) {
        APIUser apiUser = getUserFromToken(request);
        if (!apiUser.isValid()) {
            return new ResponseEntity<>(new ArrayList<>(), HttpStatus.FORBIDDEN);
        }

        List<AppItem> appItemList = new ArrayList<>();
        List<Ticket> openAgentTicketList = ticketService.findAllByMentionOpenOnly(apiUser.getUser());

        for (Ticket tkt : openAgentTicketList) {
            appItemList.add(new AppItem(
                    tkt.getId(),
                    tkt.getTicketStatus().toString(),
                    tkt.getTicketDetail(),
                    tkt.getTicketDate(),
                    0,
                    tkt.getUser().getFullName(),
                    "tickets"));
        }

        return new ResponseEntity<>(appItemList, HttpStatus.OK);
    }

    @GetMapping("/dashboard")
    public ResponseEntity<List<AppItem>> getUserDashboardCounts(HttpServletRequest request) {
        APIUser apiUser = getUserFromToken(request);
        if (!apiUser.isValid()) {
            return new ResponseEntity<>(new ArrayList<>(), HttpStatus.FORBIDDEN);
        }

        List<AppItem> itemList = new ArrayList<>();
        int counter;

        List<OrderRequest> orderRequestList = orderService.findAllBySupervisorOpenOnly(apiUser.getUser());
        counter = 0;
        for (OrderRequest order : orderRequestList) {
            counter++;
        }
        itemList.add(new AppItem("0", "Open Requests", "Requests to Fulfill",
                LocalDateTime.now(), counter, apiUser.getUser().getFullName(), "requests"));

        List<OrderItem> orderItemList = orderItemService.findAllBySupervisorOpenOnly(apiUser.getUser());
        counter = 0;
        for (OrderItem orderItem : orderItemList) {
            counter++;
        }
        itemList.add(new AppItem("1", "Open Request Items", "Items in to Fulfill",
                LocalDateTime.now(), counter, apiUser.getUser().getFullName(), "requests"));

        List<Ticket> ticketList = ticketService.findAllByAgentOpenOnly(apiUser.getUser());
        counter = 0;
        for (OrderRequest order : orderRequestList) {
            counter++;
        }
        itemList.add(new AppItem("2", "Open Tickets", "Trouble Tickets to Address",
                LocalDateTime.now(), counter, apiUser.getUser().getFullName(), "tickets"));


        List<OrderRequest> includedRequestList = orderService.findAllByMentionOpenOnly(apiUser.getUser());
        counter = 0;
        for (OrderRequest order : orderRequestList) {
            counter++;
        }
        itemList.add(new AppItem("3", "Requests Included On", "Requests to Monitor",
                LocalDateTime.now(), counter, apiUser.getUser().getFullName(), "requests"));

        List<Ticket> includedTicketList = ticketService.findAllByMentionOpenOnly(apiUser.getUser());
        counter = 0;
        for (OrderRequest order : orderRequestList) {
            counter++;
        }
        itemList.add(new AppItem("4", "Tickets Included On", "Trouble Tickets to Monitor",
                LocalDateTime.now(), counter, apiUser.getUser().getFullName(), "tickets"));

        return new ResponseEntity<>(itemList, HttpStatus.OK);
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

    @GetMapping("/requestsbyuser")
    public ResponseEntity<List<AppItem>> getRequestsByUser(HttpServletRequest request) {
        APIUser apiUser = getUserFromToken(request);
        if (!apiUser.isValid()) {
            return new ResponseEntity<>(new ArrayList<>(), HttpStatus.FORBIDDEN);
        }

        List<AppItem> appItemList = new ArrayList<>();
        for (OrderRequest item : orderService.findAllByUser(apiUser.getUser())) {
            if (!item.getOrderStatus().equals(OrderStatus.Complete) || !item.getOrderStatus().equals(OrderStatus.Cancelled)) {
                appItemList.add(new AppItem(
                        item.getId().toString(),
                        item.getOrderStatus().toString(),
                        item.getRequestNote(),
                        item.getRequestDate(),
                        item.getItemCount(),
                        item.getUser().getFullName(),
                        "requests"));
            }
        }

        return new ResponseEntity<>(appItemList, HttpStatus.OK);
    }

    @GetMapping("/requests")
    public ResponseEntity<List<AppItem>> getUserRequests(HttpServletRequest request) {
        APIUser apiUser = getUserFromToken(request);
        if (!apiUser.isValid()) {
            return new ResponseEntity<>(new ArrayList<>(), HttpStatus.FORBIDDEN);
        }

        List<OrderRequest> openSuperRequestList = orderService.findAllBySupervisorOpenOnly(apiUser.getUser());

        List<AppItem> appItemList = new ArrayList<>();
        for (OrderRequest item : orderService.findAllBySupervisorOpenOnly(apiUser.getUser())) {
            appItemList.add(new AppItem(
                    item.getId().toString(),
                    item.getOrderStatus().toString(),
                    item.getRequestNote(),
                    item.getRequestDate(),
                    item.getItemCount(),
                    item.getUser().getFullName(),
                    "requests"));
        }

        return new ResponseEntity<>(appItemList, HttpStatus.OK);
    }

    @GetMapping("/requestitems")
    public ResponseEntity<List<AppItem>> getRequestItemsById(HttpServletRequest request) {
        APIUser apiUser = getUserFromToken(request);
        if (!apiUser.isValid()) {
            return new ResponseEntity<>(new ArrayList<>(), HttpStatus.FORBIDDEN);
        }

        List<AppItem> appItemList = new ArrayList<>();
        for (OrderItem item : orderItemService.findAllBySupervisorOpenOnly(apiUser.getUser())) {
            appItemList.add(new AppItem(
                    item.getOrderRequest().getId().toString(),
                    item.getProductName(),
                    item.getOrderRequest().getRequestNote(),
                    item.getOrderRequest().getRequestDate(),
                    item.getCount(),
                    item.getOrderRequest().getUser().getFullName(),
                    "requests"));
        }


        return new ResponseEntity<>(appItemList, HttpStatus.OK);
    }

    @GetMapping("/requestmentions")
    public ResponseEntity<List<AppItem>> getRequestMentions(HttpServletRequest request) {
        APIUser apiUser = getUserFromToken(request);
        if (!apiUser.isValid()) {
            return new ResponseEntity<>(new ArrayList<>(), HttpStatus.FORBIDDEN);
        }

        List<AppItem> appItemList = new ArrayList<>();
        for (OrderRequest item : orderService.findAllByMentionOpenOnly(apiUser.getUser())) {
            appItemList.add(new AppItem(
                    item.getId().toString(),
                    item.getOrderStatus().toString(),
                    item.getRequestNote(),
                    item.getRequestDate(),
                    item.getItemCount(),
                    item.getUser().getFullName(),
                    "requests"));
        }


        return new ResponseEntity<>(appItemList, HttpStatus.OK);
    }


    @GetMapping("/request/{id}")
    public ResponseEntity<OrderRequest> getRequestById(@PathVariable BigInteger id, HttpServletRequest request) {
        APIUser apiUser = getUserFromToken(request);
        if (!apiUser.isValid()) {
            return new ResponseEntity<>(new OrderRequest(), HttpStatus.FORBIDDEN);
        }

        Optional<OrderRequest> req = orderService.findById(id);
        if (req.isEmpty()) {
            return new ResponseEntity<>(new OrderRequest(), HttpStatus.BAD_REQUEST);
        }

        return new ResponseEntity<>(req.get(), HttpStatus.OK);
    }

    @GetMapping("/itemlist/{id}")
    public ResponseEntity<List<OrderItem>> getRequestItemsById(@PathVariable BigInteger id, HttpServletRequest request) {
        APIUser apiUser = getUserFromToken(request);
        if (!apiUser.isValid()) {
            return new ResponseEntity<>(new ArrayList<>(), HttpStatus.FORBIDDEN);
        }

        Optional<OrderRequest> req = orderService.findById(id);
        if (req.isEmpty()) {
            return new ResponseEntity<>(new ArrayList<>(), HttpStatus.BAD_REQUEST);
        }

        List<OrderItem> items = req.get().getRequestItems();

        return new ResponseEntity<>(items, HttpStatus.OK);
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
