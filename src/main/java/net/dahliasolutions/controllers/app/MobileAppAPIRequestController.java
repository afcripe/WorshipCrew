package net.dahliasolutions.controllers.app;

import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import net.dahliasolutions.models.APIUser;
import net.dahliasolutions.models.AppItem;
import net.dahliasolutions.models.order.*;
import net.dahliasolutions.models.user.User;
import net.dahliasolutions.models.user.UserRoles;
import net.dahliasolutions.services.JwtService;
import net.dahliasolutions.services.order.OrderItemService;
import net.dahliasolutions.services.order.OrderNoteService;
import net.dahliasolutions.services.order.OrderService;
import net.dahliasolutions.services.user.UserRolesService;
import net.dahliasolutions.services.user.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigInteger;
import java.util.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/app/request")
public class MobileAppAPIRequestController {

    private final JwtService jwtService;
    private final UserService userService;
    private final OrderService orderService;
    private final OrderItemService orderItemService;
    private final OrderNoteService orderNoteService;
    private final UserRolesService rolesService;

    @GetMapping("/listbyuser")
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

    @GetMapping("/listbysupervisor")
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

    @GetMapping("/listitems")
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

    @GetMapping("/listbyincluded")
    public ResponseEntity<List<AppItem>> getRequestByIncluded(HttpServletRequest request) {
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

    @GetMapping("/{id}")
    public ResponseEntity<AppRequest> getRequestById(@PathVariable BigInteger id, HttpServletRequest request) {
        APIUser apiUser = getUserFromToken(request);
        AppRequest appRequest = new AppRequest();
        if (!apiUser.isValid()) {
            return new ResponseEntity<>(appRequest, HttpStatus.FORBIDDEN);
        }

        Optional<OrderRequest> req = orderService.findById(id);
        if (req.isEmpty()) {
            return new ResponseEntity<>(appRequest, HttpStatus.BAD_REQUEST);
        }

        appRequest.setAppRequestByRequest(req.get());
        appRequest.setEditable(allowRequestEdit(apiUser.getUser(), req.get()));

        return new ResponseEntity<>(appRequest, HttpStatus.OK);
    }

    @GetMapping("/itemlist/{id}")
    public ResponseEntity<List<AppRequestItem>> getRequestItemsById(@PathVariable BigInteger id, HttpServletRequest request) {
        APIUser apiUser = getUserFromToken(request);
        List<AppRequestItem> items = new ArrayList<>();
        if (!apiUser.isValid()) {
            return new ResponseEntity<>(items, HttpStatus.FORBIDDEN);
        }

        Optional<OrderRequest> req = orderService.findById(id);
        if (req.isEmpty()) {
            return new ResponseEntity<>(items, HttpStatus.BAD_REQUEST);
        }

        for (OrderItem item : req.get().getRequestItems()) {
            AppRequestItem appItem = new AppRequestItem();
            appItem.setAppItemByRequestItem(item);
            appItem.setEditable(allowRequestItemEdit(apiUser.getUser(), item));
            items.add(appItem);
        }


        return new ResponseEntity<>(items, HttpStatus.OK);
    }

    @GetMapping("/supervisorlist/{id}")
    public ResponseEntity<List<User>> getRequestSupersById(@PathVariable BigInteger id, HttpServletRequest request) {
        APIUser apiUser = getUserFromToken(request);
        if (!apiUser.isValid()) {
            return new ResponseEntity<>(new ArrayList<>(), HttpStatus.FORBIDDEN);
        }

        Optional<OrderRequest> req = orderService.findById(id);
        if (req.isEmpty()) {
            return new ResponseEntity<>(new ArrayList<>(), HttpStatus.BAD_REQUEST);
        }

        return new ResponseEntity<>(req.get().getSupervisorList(), HttpStatus.OK);
    }

    @GetMapping("/history/{id}")
    public ResponseEntity<List<OrderNote>> getRequestHistoryById(@PathVariable BigInteger id, HttpServletRequest request) {
        APIUser apiUser = getUserFromToken(request);
        if (!apiUser.isValid()) {
            return new ResponseEntity<>(new ArrayList<>(), HttpStatus.FORBIDDEN);
        }
        return new ResponseEntity<>(orderNoteService.findByOrderId(id), HttpStatus.OK);
    }

    @GetMapping("/itemorderstatus/{id}")
    public ResponseEntity<OrderItem> getRequestItemById(@PathVariable BigInteger id, HttpServletRequest request) {
        APIUser apiUser = getUserFromToken(request);
        if (!apiUser.isValid()) {
            return new ResponseEntity<>(new OrderItem(), HttpStatus.FORBIDDEN);
        }

        Optional<OrderItem> item = orderItemService.findById(id);
        if (item.isEmpty()) {
            return new ResponseEntity<>(new OrderItem(), HttpStatus.BAD_REQUEST);
        }

        return new ResponseEntity<>(item.get(), HttpStatus.OK);
    }

    @GetMapping("/orderstatusoptions")
    public ResponseEntity<List<OrderStatus>> getOrderOptions(HttpServletRequest request) {
        APIUser apiUser = getUserFromToken(request);
        if (!apiUser.isValid()) {
            return new ResponseEntity<>(new ArrayList<>(), HttpStatus.FORBIDDEN);
        }

        return new ResponseEntity<>(Arrays.asList(OrderStatus.values()), HttpStatus.OK);
    }

    @GetMapping("/supervisoroptions")
    public ResponseEntity<List<User>> getSupervisorOptions(HttpServletRequest request) {
        APIUser apiUser = getUserFromToken(request);
        if (!apiUser.isValid()) {
            return new ResponseEntity<>(new ArrayList<>(), HttpStatus.FORBIDDEN);
        }
        Collection<UserRoles> roles = getSupervisorCollection();
        List<User> userList = new ArrayList<>();
        List<User> allUsers = userService.findAll();

        for (User user : allUsers) {
            for (UserRoles role : user.getUserRoles()) {
                if (roles.contains(role)) {
                    userList.add(user);
                    break;
                }
            }
        }

        return new ResponseEntity<>(userList, HttpStatus.OK);
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

    private boolean allowRequestEdit(User user, OrderRequest request){
        Collection<UserRoles> roles = user.getUserRoles();

        for (UserRoles role : roles) {
            if (role.getName().equals("ADMIN_WRITE") || role.getName().equals("REQUEST_SUPERVISOR")) {
                return true;
            }
        }
        if ( request.getSupervisor().getId().equals(user.getId()) ) {
            return true;
        }

        return false;
    }

    private boolean allowRequestItemEdit(User user, OrderItem item){
        Collection<UserRoles> roles = user.getUserRoles();

        for (UserRoles role : roles) {
            if (role.getName().equals("ADMIN_WRITE") || role.getName().equals("REQUEST_SUPERVISOR")) {
                return true;
            }
        }
        if ( item.getSupervisor().getId().equals(user.getId()) ) {
            return true;
        }

        return false;
    }

    private Collection<UserRoles> getSupervisorCollection() {
        Collection<UserRoles> roles = new ArrayList<>();
        roles.add(rolesService.findByName("ADMIN_WRITE").get());
        roles.add(rolesService.findByName("REQUEST_WRITE").get());
        roles.add(rolesService.findByName("REQUEST_SUPERVISOR").get());
        return roles;
    }

}
