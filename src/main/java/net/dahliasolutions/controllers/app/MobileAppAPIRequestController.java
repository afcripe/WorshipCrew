package net.dahliasolutions.controllers.app;

import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import net.dahliasolutions.models.*;
import net.dahliasolutions.models.mail.EmailDetails;
import net.dahliasolutions.models.order.*;
import net.dahliasolutions.models.records.*;
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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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

    @GetMapping("/allopen")
    public ResponseEntity<List<AppItem>> getRequestsAllOpen(HttpServletRequest request) {
        APIUser apiUser = getUserFromToken(request);
        if (!apiUser.isValid()) {
            return new ResponseEntity<>(new ArrayList<>(), HttpStatus.FORBIDDEN);
        }

        List<AppItem> appItemList = new ArrayList<>();
        for (OrderRequest item : orderService.findAll()) {
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

    @GetMapping("/{id}")
    public ResponseEntity<AppRequest> getRequestById(@PathVariable BigInteger id, HttpServletRequest request) {
        APIUser apiUser = getUserFromToken(request);
        AppRequest appRequest = new AppRequest();
        if (!apiUser.isValid()) {
            return new ResponseEntity<>(appRequest, HttpStatus.FORBIDDEN);
        }

        Optional<OrderRequest> req = orderService.findById(id);
        if (req.isEmpty()) {
            return new ResponseEntity<>(appRequest, HttpStatus.NOT_FOUND);
        }

        appRequest.setAppRequestByRequest(req.get());
        appRequest.setEditable(allowRequestEdit(apiUser.getUser(), req.get()));

        return new ResponseEntity<>(appRequest, HttpStatus.OK);
    }

    @GetMapping("/item/{id}")
    public ResponseEntity<AppRequestItem> getRequestItemById(@PathVariable BigInteger id, HttpServletRequest request) {
        APIUser apiUser = getUserFromToken(request);
        List<AppRequestItem> items = new ArrayList<>();
        if (!apiUser.isValid()) {
            return new ResponseEntity<>(new AppRequestItem(), HttpStatus.FORBIDDEN);
        }

        Optional<OrderItem> item = orderItemService.findById(id);
        if (item.isEmpty()) {
            return new ResponseEntity<>(new AppRequestItem(), HttpStatus.NOT_FOUND);
        }

        AppRequestItem appItem = new AppRequestItem();
        appItem.setAppItemByRequestItem(item.get());
        appItem.setEditable(allowRequestItemEdit(apiUser.getUser(), item.get()));



        return new ResponseEntity<>(appItem, HttpStatus.OK);
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
            return new ResponseEntity<>(items, HttpStatus.NOT_FOUND);
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
            return new ResponseEntity<>(new ArrayList<>(), HttpStatus.NOT_FOUND);
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

    @PostMapping("/getacknowledge")
    public ResponseEntity<SingleStringModel> getRequestAcknowledge(@ModelAttribute SingleBigIntegerModel requestModel, HttpServletRequest request) {
        APIUser apiUser = getUserFromToken(request);
        if (!apiUser.isValid()) {
            return new ResponseEntity<>(new SingleStringModel("Access denied!"), HttpStatus.FORBIDDEN);
        }

        Optional<OrderRequest> orderRequest = orderService.findById(requestModel.id());
        if (orderRequest.isEmpty()) {
            return new ResponseEntity<>(new SingleStringModel("Not Found"), HttpStatus.NOT_FOUND);
        }

        if (orderRequest.get().getOrderStatus().equals(OrderStatus.Submitted)
                && orderRequest.get().getSupervisor().getId().equals(apiUser.getUser().getId())) {
            return new ResponseEntity<>(new SingleStringModel("Acknowledge"), HttpStatus.OK);
        }

        return new ResponseEntity<>(new SingleStringModel("Not Supervisor"), HttpStatus.BAD_REQUEST);
    }

    @PostMapping("/setacknowledge")
    public ResponseEntity<SingleStringModel> setRequestAcknowledge(@ModelAttribute SingleBigIntegerModel requestModel, HttpServletRequest request) {
        APIUser apiUser = getUserFromToken(request);
        if (!apiUser.isValid()) {
            return new ResponseEntity<>(new SingleStringModel("Access denied!"), HttpStatus.FORBIDDEN);
        }

        Optional<OrderRequest> orderRequest = orderService.findById(requestModel.id());
        if (orderRequest.isEmpty()) {
            return new ResponseEntity<>(new SingleStringModel("Not Found"), HttpStatus.NOT_FOUND);
        }
        orderRequest.get().setOrderStatus(OrderStatus.Received);
        orderService.save(orderRequest.get());
        OrderNote orderNote = orderNoteService.createOrderNote(new OrderNote(
                null,
                orderRequest.get().getId(),
                null,
                "Request marked received. ",
                BigInteger.valueOf(0),
                OrderStatus.Received,
                apiUser.getUser()));

        return new ResponseEntity<>(new SingleStringModel("success"), HttpStatus.OK);
    }

    @GetMapping("/itemorderstatus/{id}")
    public ResponseEntity<AppRequestItem> getRequestItemStatusById(@PathVariable BigInteger id, HttpServletRequest request) {
        APIUser apiUser = getUserFromToken(request);
        if (!apiUser.isValid()) {
            return new ResponseEntity<>(new AppRequestItem(), HttpStatus.FORBIDDEN);
        }

        Optional<OrderItem> item = orderItemService.findById(id);
        if (item.isEmpty()) {
            return new ResponseEntity<>(new AppRequestItem(), HttpStatus.NOT_FOUND);
        }

        AppRequestItem appItem = new AppRequestItem();
        appItem.setAppItemByRequestItem(item.get());

        return new ResponseEntity<>(appItem, HttpStatus.OK);
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

    @PostMapping("/changerequeststatus")
    public ResponseEntity<SingleStringModel> updateRequestStatus(@ModelAttribute ChangeStatusModel statusModel, HttpServletRequest request) {
        APIUser apiUser = getUserFromToken(request);
        if (!apiUser.isValid()) {
            return new ResponseEntity<>(new SingleStringModel(""), HttpStatus.FORBIDDEN);
        }

        OrderStatus setStatus = OrderStatus.valueOf(statusModel.requestStatus());
        Optional<OrderRequest> orderRequest = orderService.findById(statusModel.requestId());

        if (orderRequest.isEmpty()) {
            return new ResponseEntity<>(new SingleStringModel(""), HttpStatus.NOT_FOUND);
        }

        if (orderRequest.isPresent()) {
            orderRequest.get().setOrderStatus(OrderStatus.valueOf(statusModel.requestStatus()));
            orderService.save(orderRequest.get());
            OrderNote orderNote = orderNoteService.createOrderNote(new OrderNote(
                    null,
                    orderRequest.get().getId(),
                    null,
                    statusModel.requestNote(),
                    BigInteger.valueOf(0),
                    setStatus,
                    apiUser.getUser()));
        }

        return new ResponseEntity<>(new SingleStringModel(setStatus.toString()), HttpStatus.OK);
    }

    @PostMapping("/changeitemstatus")
    public ResponseEntity<SingleStringModel> updateItemStatus(@ModelAttribute ChangeStatusModel statusModel, HttpServletRequest request) {
        APIUser apiUser = getUserFromToken(request);
        if (!apiUser.isValid()) {
            return new ResponseEntity<>(new SingleStringModel(""), HttpStatus.FORBIDDEN);
        }

        OrderStatus setStatus = OrderStatus.valueOf(statusModel.requestStatus());
        Optional<OrderItem> requestItem = orderItemService.findById(statusModel.requestId());

        if (requestItem.isEmpty()) {
            return new ResponseEntity<>(new SingleStringModel(""), HttpStatus.NOT_FOUND);
        }

        String noteDetail = statusModel.requestNote();
        if (requestItem.isPresent()) {
            // update item status
            requestItem.get().setItemStatus(setStatus);
            orderItemService.save(requestItem.get());
            // add new note to order
            if (noteDetail.equals("")) {
                noteDetail = requestItem.get().getSupervisor().getFirstName() + " " + requestItem.get().getSupervisor().getLastName() +
                        " updated the status of " + requestItem.get().getProductName() + " to " + statusModel.requestStatus() + ".";
            }
            OrderNote orderNote = orderNoteService.createOrderNote(new OrderNote(
                    null,
                    requestItem.get().getOrderRequest().getId(),
                    null,
                    noteDetail,
                    requestItem.get().getId(),
                    requestItem.get().getItemStatus(),
                    apiUser.getUser()));

        }

        return new ResponseEntity<>(new SingleStringModel(setStatus.toString()), HttpStatus.OK);
    }

    @PostMapping("/addsupervisor")
    public ResponseEntity<SingleStringModel> addSupervisorToRequest(@ModelAttribute AddSupervisorModel supervisorModel, HttpServletRequest request) {
        APIUser apiUser = getUserFromToken(request);
        if (!apiUser.isValid()) {
            return new ResponseEntity<>(new SingleStringModel(""), HttpStatus.FORBIDDEN);
        }

        Optional<OrderRequest> orderRequest = orderService.findById(supervisorModel.requestId());
        Optional<User> newSuper = userService.findById(supervisorModel.userId());

        if (orderRequest.isEmpty()) {
            return new ResponseEntity<>(new SingleStringModel(""), HttpStatus.NOT_FOUND);
        }

        String noteDetail = "";
        if (orderRequest.isPresent()) {
            if (newSuper.isPresent()) {
                if (supervisorModel.primary()) {
                    noteDetail = newSuper.get().getFirstName()+" "+newSuper.get().getLastName()+" was set as supervisor of the request.";

                    orderRequest.get().setSupervisorList(
                            removeFromSupervisorList(newSuper.get(), orderRequest.get().getSupervisorList()));
                    orderRequest.get().setSupervisorList(
                            addToSupervisorList(orderRequest.get().getSupervisor(), orderRequest.get().getSupervisorList()));

                    orderRequest.get().setSupervisor(newSuper.get());
                    orderService.save(orderRequest.get());
                    orderNoteService.createOrderNote(new OrderNote(
                            null,
                            orderRequest.get().getId(),
                            null,
                            noteDetail,
                            BigInteger.valueOf(0),
                            orderRequest.get().getOrderStatus(),
                            apiUser.getUser()));
                } else {
                    noteDetail = newSuper.get().getFirstName()+" "+newSuper.get().getLastName()+" was add to the request.";

                    orderRequest.get().setSupervisorList(
                            addToSupervisorList(newSuper.get(), orderRequest.get().getSupervisorList()));

                    orderService.save(orderRequest.get());
                    orderNoteService.createOrderNote(new OrderNote(
                            null,
                            orderRequest.get().getId(),
                            null,
                            noteDetail,
                            BigInteger.valueOf(0),
                            orderRequest.get().getOrderStatus(),
                            apiUser.getUser()));
                }
            }
        }
        return new ResponseEntity<>(new SingleStringModel(newSuper.get().getFullName()), HttpStatus.OK);
    }

    @PostMapping("/removesupervisor")
    public ResponseEntity<SingleStringModel> removeSupervisorToRequest(@ModelAttribute AddSupervisorModel supervisorModel, HttpServletRequest request) {
        APIUser apiUser = getUserFromToken(request);
        if (!apiUser.isValid()) {
            return new ResponseEntity<>(new SingleStringModel(""), HttpStatus.FORBIDDEN);
        }

        Optional<OrderRequest> orderRequest = orderService.findById(supervisorModel.requestId());
        Optional<User> newSuper = userService.findById(supervisorModel.userId());

        if (orderRequest.isEmpty()) {
            return new ResponseEntity<>(new SingleStringModel(""), HttpStatus.NOT_FOUND);
        }

        boolean required = false;

        String noteDetail = "";
        if (orderRequest.isPresent()) {
            // determine if user is required
            for (OrderItem item : orderRequest.get().getRequestItems()) {
                if (item.getSupervisor().equals(newSuper.get())) {
                    required = true;
                }
            }

            if (newSuper.isPresent() && !required) {
                noteDetail = newSuper.get().getFirstName()+" "+newSuper.get().getLastName()+" was removed from request.";

                orderRequest.get().setSupervisorList(
                        removeFromSupervisorList(newSuper.get(), orderRequest.get().getSupervisorList()));

                orderService.save(orderRequest.get());
                orderNoteService.createOrderNote(new OrderNote(
                        null,
                        orderRequest.get().getId(),
                        null,
                        noteDetail,
                        BigInteger.valueOf(0),
                        orderRequest.get().getOrderStatus(),
                        apiUser.getUser()));
                return new ResponseEntity<>(new SingleStringModel(newSuper.get().getFullName()), HttpStatus.OK);
            }
        }
        return new ResponseEntity<>(new SingleStringModel(newSuper.get().getFullName()), HttpStatus.NOT_FOUND);
    }

    @PostMapping("/changeitemsupervisor")
    public ResponseEntity<SingleStringModel> addSupervisorToItem(@ModelAttribute AddSupervisorModel supervisorModel, HttpServletRequest request) {
        APIUser apiUser = getUserFromToken(request);
        if (!apiUser.isValid()) {
            return new ResponseEntity<>(new SingleStringModel(""), HttpStatus.FORBIDDEN);
        }

        Optional<OrderItem> requestItem = orderItemService.findById(supervisorModel.requestId());
        Optional<User> newSuper = userService.findById(supervisorModel.userId());

        if (requestItem.isEmpty()) {
            return new ResponseEntity<>(new SingleStringModel(""), HttpStatus.NOT_FOUND);
        }

        String noteDetail = "";
        if (requestItem.isPresent()) {
            if (newSuper.isPresent()) {
                // save new super to request item and change item status to submitted
                requestItem.get().setSupervisor(newSuper.get());
                requestItem.get().setItemStatus(OrderStatus.Submitted);
                orderItemService.save(requestItem.get());
                // add new super to list if not primary
                if (!requestItem.get().getOrderRequest().getSupervisor().equals(newSuper.get())) {
                    requestItem.get().getOrderRequest().setSupervisorList(
                            addToSupervisorList(newSuper.get(), requestItem.get().getOrderRequest().getSupervisorList()));
                }
                // save request with updated super list
                orderService.save(requestItem.get().getOrderRequest());
                // create new note for request
                noteDetail = newSuper.get().getFirstName()+" "+newSuper.get().getLastName()+
                        " was assigned to fulfill "+requestItem.get().getProductName()+".";
                orderNoteService.createOrderNote(new OrderNote(
                        null,
                        requestItem.get().getOrderRequest().getId(),
                        null,
                        noteDetail,
                        requestItem.get().getId(),
                        requestItem.get().getItemStatus(),
                        apiUser.getUser()));

                return new ResponseEntity<>(new SingleStringModel(newSuper.get().getFullName()), HttpStatus.OK);
            }
        }
        return new ResponseEntity<>(new SingleStringModel(newSuper.get().getFullName()), HttpStatus.NOT_FOUND);
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

    private List<User> removeFromSupervisorList(User user, List<User> list){
        for (User su : list) {
            if (su.getId().equals(user.getId())) {
                list.remove(su);
                break;
            }
        }
        return list;
    }

    private List<User> addToSupervisorList(User user, List<User> list){
        for (User su : list) {
            if (su.getId().equals(user.getId())) {
                return list;
            }
        }
        list.add(user);
        return list;
    }

}
