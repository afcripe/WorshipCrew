package net.dahliasolutions.controllers.app;

import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import net.dahliasolutions.controllers.AuthenticationResponse;
import net.dahliasolutions.models.APIUser;
import net.dahliasolutions.models.AppItem;
import net.dahliasolutions.models.LoginModel;
import net.dahliasolutions.models.UniversalAppSearchModel;
import net.dahliasolutions.models.order.*;
import net.dahliasolutions.models.support.*;
import net.dahliasolutions.models.user.User;
import net.dahliasolutions.services.AuthService;
import net.dahliasolutions.services.JwtService;
import net.dahliasolutions.services.order.OrderItemService;
import net.dahliasolutions.services.order.OrderService;
import net.dahliasolutions.services.support.TicketService;
import net.dahliasolutions.services.user.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigInteger;
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


        List<OrderRequest> includedRequestList = orderService.findAllByMentionOpenOnly(apiUser.getUser());
        counter = 0;
        for (OrderRequest order : orderRequestList) {
            counter++;
        }
        itemList.add(new AppItem("2", "Requests Included On", "Requests to Monitor",
                LocalDateTime.now(), counter, apiUser.getUser().getFullName(), "requests"));

        List<Ticket> ticketList = ticketService.findAllByAgentOpenOnly(apiUser.getUser());
        counter = 0;
        for (OrderRequest order : orderRequestList) {
            counter++;
        }
        itemList.add(new AppItem("3", "Open Tickets", "Trouble Tickets to Address",
                LocalDateTime.now(), counter, apiUser.getUser().getFullName(), "tickets"));

        List<Ticket> includedTicketList = ticketService.findAllByMentionOpenOnly(apiUser.getUser());
        counter = 0;
        for (OrderRequest order : orderRequestList) {
            counter++;
        }
        itemList.add(new AppItem("4", "Tickets Included On", "Trouble Tickets to Monitor",
                LocalDateTime.now(), counter, apiUser.getUser().getFullName(), "tickets"));

        return new ResponseEntity<>(itemList, HttpStatus.OK);
    }

    @GetMapping("/dashboarduseritems")
    public ResponseEntity<List<AppItem>> getUserDashboardItemsCount(HttpServletRequest request) {
        APIUser apiUser = getUserFromToken(request);
        if (!apiUser.isValid()) {
            return new ResponseEntity<>(new ArrayList<>(), HttpStatus.FORBIDDEN);
        }

        List<AppItem> itemList = new ArrayList<>();
        int counter;

        List<OrderRequest> orderRequestList = orderService.findAllByUser(apiUser.getUser());
        counter = 0;
        for (OrderRequest order : orderRequestList) {
            counter++;
        }
        itemList.add(new AppItem("0", "My Requests", "Open Requests",
                LocalDateTime.now(), counter, apiUser.getUser().getFullName(), "requests"));

        List<Ticket> ticketList = ticketService.findAllByUser(apiUser.getUser());
        counter = 0;
        for (OrderRequest order : orderRequestList) {
            counter++;
        }
        itemList.add(new AppItem("1", "My Tickets", "Open Tickets",
                LocalDateTime.now(), counter, apiUser.getUser().getFullName(), "tickets"));

        return new ResponseEntity<>(itemList, HttpStatus.OK);
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
            try {
                Optional<User> currentUser = userService.findByUsername(jwtService.extractUsername(token));
                if (currentUser.isPresent()) {
                    if (jwtService.isTokenValid(token, currentUser.get())) {
                        return new APIUser(true, currentUser.get());
                    }
                }
            } catch (ExpiredJwtException e) {
                System.out.println("Token Expired");
            }
        }
        return new APIUser(false, new User());
    }

}
