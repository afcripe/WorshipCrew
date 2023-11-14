package net.dahliasolutions.controllers.app;

import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import net.dahliasolutions.controllers.AuthenticationResponse;
import net.dahliasolutions.models.*;
import net.dahliasolutions.models.order.*;
import net.dahliasolutions.models.records.SingleStringModel;
import net.dahliasolutions.models.support.*;
import net.dahliasolutions.models.user.*;
import net.dahliasolutions.models.wiki.WikiPost;
import net.dahliasolutions.services.AuthService;
import net.dahliasolutions.services.JwtService;
import net.dahliasolutions.services.order.OrderItemService;
import net.dahliasolutions.services.order.OrderService;
import net.dahliasolutions.services.support.TicketService;
import net.dahliasolutions.services.user.EndpointService;
import net.dahliasolutions.services.user.UserNotificationSubscribeService;
import net.dahliasolutions.services.user.UserService;
import net.dahliasolutions.services.wiki.WikiPostService;
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
    private final WikiPostService wikiPostService;
    private final OrderItemService orderItemService;
    private final EndpointService endpointService;
    private final UserNotificationSubscribeService userSubscribeService;

    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponse> getAuthUser(@ModelAttribute LoginModel loginModel) {
        AuthenticationResponse response = authService.authenticate(loginModel);
        if (response.isLoggedIn()) {
            return new ResponseEntity<>(response, HttpStatus.OK);
        }
        return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
    }

    @GetMapping("/getmodules")
    public ResponseEntity<AppUserPermissionModel> getUseModules(HttpServletRequest request) {
        APIUser apiUser = getUserFromToken(request);
        AppUserPermissionModel mods = new AppUserPermissionModel();
        if (!apiUser.isValid()) {
            return new ResponseEntity<>(mods, HttpStatus.FORBIDDEN);
        }

        Collection<UserRoles> roles = apiUser.getUser().getUserRoles();
        for (UserRoles role : roles){
            if (role.getName().equals("ADMIN_WRITE") || role.getName().equals("USER_SUPERVISOR")
                    || role.getName().equals("USER_WRITE") || role.getName().equals("USER_READ")) {
                mods.setUserMod(true);
            }
            if (role.getName().equals("ADMIN_WRITE") || role.getName().equals("REQUEST_SUPERVISOR")
                    || role.getName().equals("REQUEST_WRITE")) {
                mods.setRequestMod(true);
            }
            if (role.getName().equals("ADMIN_WRITE") || role.getName().equals("SUPPORT_SUPERVISOR")
                    || role.getName().equals("SUPPORT_AGENT") || role.getName().equals("SUPPORT_READ")) {
                mods.setTicketMod(true);
            }
            if (role.getName().equals("ADMIN_WRITE") || role.getName().equals("RESOURCE_SUPERVISOR")
                    || role.getName().equals("RESOURCE_WRITE") || role.getName().equals("RESOURCE_READ")) {
                mods.setResourceMod(true);
            }
            mods.setMessagesMod(true);
        }

        return new ResponseEntity<>(mods, HttpStatus.OK);
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

    @PostMapping("/swtoken")
    public ResponseEntity<SingleStringModel> setSWToken(@ModelAttribute EndpointModel endpoint, HttpServletRequest request) {
        APIUser apiUser = getUserFromToken(request);
        if (!apiUser.isValid()) {
            return new ResponseEntity<>(new SingleStringModel(""), HttpStatus.FORBIDDEN);
        }

        Optional<UserEndpoint> existingEP = endpointService.findByToken(endpoint.token());
        if (existingEP.isPresent()) {
            addUserSubscription(apiUser.getUser());
            return new ResponseEntity<>(new SingleStringModel("exists"), HttpStatus.OK);
        }

        UserEndpoint newEP = endpointService.save(new UserEndpoint(null, endpoint.name(), endpoint.token(), apiUser.getUser()));
        apiUser.getUser().getEndpoints().add(newEP);
        userService.save(apiUser.getUser());

        addUserSubscription(apiUser.getUser());

        System.out.println(endpoint.token());
        return new ResponseEntity<>(new SingleStringModel("success"), HttpStatus.OK);
    }

    @PostMapping("/removetoken")
    public ResponseEntity<SingleStringModel> setRemoveToken(@ModelAttribute EndpointModel endpoint, HttpServletRequest request) {
        APIUser apiUser = getUserFromToken(request);
        if (!apiUser.isValid()) {
            return new ResponseEntity<>(new SingleStringModel(""), HttpStatus.FORBIDDEN);
        }

        Optional<UserEndpoint> existingEP = endpointService.findByToken(endpoint.token());
        if (existingEP.isPresent()) {
            apiUser.getUser().getEndpoints().remove(existingEP.get());
            userService.save(apiUser.getUser());
            removeUserSubscription(apiUser.getUser());
        }

        return new ResponseEntity<>(new SingleStringModel("success"), HttpStatus.OK);
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
        List<User> userList = userList(apiUser.getUser());
        for (User user : users) {
            // only send users allowed to be viewed
            if (userList.contains(user)) {
                r.add(new UniversalAppSearchModel("user", user.getId().toString(), user.getFullName(), ""));
            }
        }

        // search for tickets
        List<Ticket> tickets = ticketService.searchAllById(searchTerm);
        for (Ticket ticket : tickets) {
            r.add(new UniversalAppSearchModel("ticket", ticket.getId(), ticket.getUser().getFullName(), ticket.getTicketDetail()));
        }

        // search for resource
        List<WikiPost> wikiPosts = wikiPostService.searchByTitle(searchTerm);
        for (WikiPost post : wikiPosts) {
            // only include if user has permission
            if (positionFilterPost(apiUser.getUser(), post)) {
                r.add(new UniversalAppSearchModel("resource", post.getId().toString(), post.getTitle(), post.getSummary()));
            }
        }


        boolean isNumber;
        try {
            Integer id = Integer.parseInt(searchTerm);
            isNumber = true;
        } catch (NumberFormatException nfe) {
            isNumber = false;
        }
        // search for request
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

    private void addUserSubscription(User user) {
        Optional<UserNotificationSubscribe> subscription =
                userSubscribeService.findByEndPointAndUser(NotificationEndPoint.Push, user);
        if (subscription.isEmpty()) {
            UserNotificationSubscribe subscribe = userSubscribeService.save(
                    new UserNotificationSubscribe(null, NotificationEndPoint.Push, user));
            user.getSubscriptions().add(subscribe);
            userService.save(user);
        }
    }

    private void removeUserSubscription(User user) {
        List<UserEndpoint> endpoints = user.getEndpoints();
        Optional<UserNotificationSubscribe> subscription =
                userSubscribeService.findByEndPointAndUser(NotificationEndPoint.Push, user);
        if (endpoints.isEmpty() && subscription.isPresent()) {
            user.getSubscriptions().remove(subscription.get());
            if (user.getSubscriptions().size()<2) {
                user.setEnableEmailSubscription(true);
            }
            userService.save(user);
            try {
                userSubscribeService.deleteBySubscription(subscription.get());
            } catch (Exception e) {
                System.out.println(e);
            }
        }
    }

    private boolean positionFilterPost(User currentUser, WikiPost post) {
        Collection<UserRoles> roles = currentUser.getUserRoles();
        for (UserRoles role : roles){
            if (role.getName().equals("ADMIN_WRITE") || role.getName().equals("RESOURCE_SUPERVISOR")) {
                return true;
            }
        }
        if (post.getPositionList().isEmpty()) {
            return true;
        }
        if (post.getPositionList().contains(currentUser.getPosition())) {
            return true;
        }
        if (post.getAuthor().getId().equals(currentUser.getId())) {
            return true;
        }
        return false;
    }

    private List<WikiPost> positionFilterList(User currentUser, List<WikiPost> posts) {
        List<WikiPost> returnList = new ArrayList<>();

        Collection<UserRoles> roles = currentUser.getUserRoles();
        for (WikiPost post : posts) {
            for (UserRoles role : roles){
                if (role.getName().equals("ADMIN_WRITE") || role.getName().equals("RESOURCE_SUPERVISOR")) {
                    if (!returnList.contains(post)) {
                        returnList.add(post);
                    }
                }
            }
            if (post.getPositionList().isEmpty()) {
                if (!returnList.contains(post)) {
                    returnList.add(post);
                }
            }
            if (post.getPositionList().contains(currentUser.getPosition())) {
                if (!returnList.contains(post)) {
                    returnList.add(post);
                }
            }
            if (post.getAuthor().getId().equals(currentUser.getId())) {
                if (!returnList.contains(post)) {
                    returnList.add(post);
                }
            }
        }
        return returnList;
    }

    private List<User> userList(User currentUser) {
        String permission = permissionType(currentUser);
        List<User> userList;
        switch (permission){
            case "All Users":
                userList = userService.findAll();
                break;
            case "Department Users":
                userList = userService.findAllByDepartmentAndDeleted(currentUser.getDepartment().getRegionalDepartment(), false);
                break;
            case "Campus Users":
                userList = userService.findAllByCampus(currentUser.getCampus());
                break;
            default:
                userList = userService.findAllByDepartmentCampus(currentUser.getDepartment());
        }
        return userList;
    }

    private String permissionType(User currentUser) {
        Collection<UserRoles> roles = currentUser.getUserRoles();
        String typeString = "Campus Users";
        int priority = 0;
        for (UserRoles role : roles) {
            if (role.getName().equals("USER_WRITE") || role.getName().equals("USER_READ")) {
                if (priority < 1) {
                    typeString = "Campus Department Users";
                    priority = 1;
                }
            } else if (role.getName().equals("CAMPUS_WRITE") || role.getName().equals("CAMPUS_READ")) {
                if (priority < 2) {
                    typeString = "Campus Users";
                    priority = 2;
                }
            }   else if (role.getName().equals("DIRECTOR_WRITE") || role.getName().equals("DIRECTOR_READ")) {
                if (priority < 3) {
                    typeString = "Department Users";
                    priority = 3;
                }
            } else if (role.getName().equals("ADMIN_WRITE") || role.getName().equals("USER_SUPERVISOR")) {
                if (priority < 4) {
                    typeString = "All Users";
                    priority = 4;
                }
            }
        }
        return typeString;
    }

}
