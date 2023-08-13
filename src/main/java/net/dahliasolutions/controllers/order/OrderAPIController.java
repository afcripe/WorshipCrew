package net.dahliasolutions.controllers.order;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import net.dahliasolutions.models.*;
import net.dahliasolutions.models.mail.EmailDetails;
import net.dahliasolutions.models.order.OrderItem;
import net.dahliasolutions.models.order.OrderNote;
import net.dahliasolutions.models.order.OrderRequest;
import net.dahliasolutions.models.order.OrderStatus;
import net.dahliasolutions.models.store.Cart;
import net.dahliasolutions.models.user.User;
import net.dahliasolutions.models.user.UserRoles;
import net.dahliasolutions.services.mail.EmailService;
import net.dahliasolutions.services.order.OrderItemService;
import net.dahliasolutions.services.order.OrderNoteService;
import net.dahliasolutions.services.order.OrderService;
import net.dahliasolutions.services.user.UserRolesService;
import net.dahliasolutions.services.user.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigInteger;
import java.util.*;

@RestController
@RequestMapping("/api/v1/request")
@RequiredArgsConstructor
public class OrderAPIController {

    private final OrderService orderService;
    private final OrderItemService orderItemService;
    private final OrderNoteService orderNoteService;
    private final UserService userService;
    private final UserRolesService rolesService;
    private final EmailService emailService;

    @GetMapping("")
    public String getOrders() { return null; }

    @PostMapping("/cancel")
    public SingleBigIntegerModel cancelOrder(@ModelAttribute SingleBigIntegerModel integerModel) {
        Optional<OrderRequest> orderRequest = orderService.findById(integerModel.id());
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) auth.getPrincipal();

        OrderStatus newStatus = OrderStatus.Cancelled;
        String newNote = "User Cancelled Order";

        if (orderRequest.isPresent()) {
            // if an item in request is complete, mark complete, otherwise cancel
            for (OrderItem item : orderRequest.get().getRequestItems()) {
                if (item.getItemStatus().equals(OrderStatus.Complete)) {
                    newStatus = OrderStatus.Complete;
                    newNote = "Request is complete. Balance of items cancelled by user";
                }
            }
            if (!orderRequest.get().getOrderStatus().equals("Cancelled") || !orderRequest.get().getOrderStatus().equals("Complete")) {
                OrderNote orderNote = new OrderNote(
                        null,
                        orderRequest.get().getId(),
                        null,
                        newNote,
                        BigInteger.valueOf(0),
                        newStatus,
                        user);
                orderNote = orderNoteService.createOrderNote(orderNote);
                orderRequest.get().setOrderStatus(orderNote.getOrderStatus());
                orderRequest.get().setRequestNote(orderNote.getOrderNote());
                for (OrderItem item : orderRequest.get().getRequestItems()) {
                    if (!item.getItemStatus().equals(OrderStatus.Complete)) {
                        item.setItemStatus(OrderStatus.Cancelled);
                    }
                }
                orderService.save(orderRequest.get());

                EmailDetails emailDetailsUser =
                        new EmailDetails(orderRequest.get().getSupervisor().getContactEmail(),"Request Cancelled", "", null );
                BrowserMessage returnMsg = emailService.sendUserRequest(emailDetailsUser, orderRequest.get());

                EmailDetails emailDetailsSupervisor =
                        new EmailDetails(orderRequest.get().getSupervisor().getContactEmail(),"A Request Has Been Cancelled", "", null );
                BrowserMessage returnMsg2 = emailService.sendUserRequest(emailDetailsSupervisor, orderRequest.get());
            }
        }

        return integerModel;
    }

    @GetMapping("/getstatusoptions")
    public List<OrderStatus> getStatusOptions(){
        List<OrderStatus> statuses = new ArrayList<OrderStatus>(Arrays.asList(OrderStatus.values()));
        return new ArrayList<OrderStatus>(Arrays.asList(OrderStatus.values()));
    }

    @GetMapping("/getsupervisors")
    public List<User> getSupervisors(){
        Collection<UserRoles> roles = getSupervisorCollection();
        boolean addUser = false;
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
        return userList;
    }

    @PostMapping("/changestatus")
    public ChangeStatusModel updateRequestStatus(@ModelAttribute ChangeStatusModel statusModel) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) auth.getPrincipal();
        Optional<OrderRequest> orderRequest = orderService.findById(statusModel.requestId());
        if (orderRequest.isPresent()) {
            orderRequest.get().setOrderStatus(OrderStatus.valueOf(statusModel.requestStatus()));
//            orderRequest.get().setRequestNote(statusModel.RequestNote());
            orderService.save(orderRequest.get());
            orderNoteService.createOrderNote(new OrderNote(
                    null,
                    orderRequest.get().getId(),
                    null,
                    statusModel.RequestNote(),
                    BigInteger.valueOf(0),
                    OrderStatus.valueOf(statusModel.requestStatus()),
                    user));
        }

        EmailDetails emailDetailsUser =
                new EmailDetails(user.getContactEmail(),"Your Request Has Been Updated", "", null );
        BrowserMessage returnMsg = emailService.sendUserRequest(emailDetailsUser, orderRequest.get());

        return statusModel;
    }

    @PostMapping("/changeitemstatus")
    public ChangeStatusModel updateItemStatus(@ModelAttribute ChangeStatusModel statusModel) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) auth.getPrincipal();
        Optional<OrderItem> requestItem = orderItemService.findById(statusModel.requestId());
        String noteDetail = "";
        if (requestItem.isPresent()) {
            // update item status
            requestItem.get().setItemStatus(OrderStatus.valueOf(statusModel.requestStatus()));
            orderItemService.save(requestItem.get());
            // add new note to order
            noteDetail = requestItem.get().getSupervisor().getFirstName()+" "+requestItem.get().getSupervisor().getLastName()+
                    " updated the status of "+requestItem.get().getProductName()+" to "+statusModel.requestStatus()+".";
            orderNoteService.createOrderNote(new OrderNote(
                    null,
                    requestItem.get().getOrderRequest().getId(),
                    null,
                    noteDetail,
                    requestItem.get().getId(),
                    requestItem.get().getItemStatus(),
                    user));
            // Email everyone
            EmailDetails emailDetailsUser =
                    new EmailDetails(requestItem.get().getOrderRequest().getUser().getContactEmail(),"The Status of a Request Item Changed", "", null );
            BrowserMessage returnMsg = emailService.sendUserRequest(emailDetailsUser, requestItem.get().getOrderRequest());

            EmailDetails emailDetailsSupervisor =
                    new EmailDetails(requestItem.get().getOrderRequest().getSupervisor().getContactEmail(),"The Status of a Request Item Changed", "", null );
            BrowserMessage returnMsg2 = emailService.sendUserRequest(emailDetailsSupervisor, requestItem.get().getOrderRequest());
        }

        return statusModel;
    }

    @PostMapping("/addsupervisor")
    public AddSupervisorModel addSupervisorToRequest(@ModelAttribute AddSupervisorModel supervisorModel) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) auth.getPrincipal();

        Optional<OrderRequest> orderRequest = orderService.findById(supervisorModel.requestId());
        Optional<User> newSuper = userService.findById(supervisorModel.userId());
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
                            user));
                } else {
                    noteDetail = newSuper.get().getFirstName()+" "+newSuper.get().getLastName()+" was add to the request.";

                    orderRequest.get().setSupervisorList(
                            addToSupervisorList(orderRequest.get().getSupervisor(), orderRequest.get().getSupervisorList()));

                    orderService.save(orderRequest.get());
                    orderNoteService.createOrderNote(new OrderNote(
                            null,
                            orderRequest.get().getId(),
                            null,
                            noteDetail,
                            BigInteger.valueOf(0),
                            orderRequest.get().getOrderStatus(),
                            user));
                }
            }
        }
            return supervisorModel;
    }

    @PostMapping("/removesupervisor")
    public AddSupervisorModel removeSupervisorToRequest(@ModelAttribute AddSupervisorModel supervisorModel, HttpSession session) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) auth.getPrincipal();

        Optional<OrderRequest> orderRequest = orderService.findById(supervisorModel.requestId());
        Optional<User> newSuper = userService.findById(supervisorModel.userId());
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
                        user));
            } else {
                session.setAttribute("msgError", "Cannot remove someone who is assigned to request.");
                return new AddSupervisorModel(BigInteger.valueOf(0), BigInteger.valueOf(0), false);
            }
        }
        return supervisorModel;
    }

    @PostMapping("/changeitemsupervisor")
    public AddSupervisorModel addSupervisorToItem(@ModelAttribute AddSupervisorModel supervisorModel) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) auth.getPrincipal();

        Optional<OrderItem> requestItem = orderItemService.findById(supervisorModel.requestId());
        Optional<User> newSuper = userService.findById(supervisorModel.userId());
        String noteDetail = "";
        if (requestItem.isPresent()) {
            if (newSuper.isPresent()) {
                // save new super to request item
                requestItem.get().setSupervisor(newSuper.get());
                orderItemService.save(requestItem.get());
                // add new super to list if not primary
                if (!requestItem.get().getOrderRequest().getSupervisor().equals(newSuper.get())) {
                    requestItem.get().getOrderRequest().setSupervisorList(
                            addToSupervisorList(newSuper.get(), requestItem.get().getOrderRequest().getSupervisorList()));
                }
                // save request with updated supre list
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
                        user));
                // email new supervisor
                EmailDetails emailDetailsSupervisor =
                        new EmailDetails(newSuper.get().getContactEmail(),"You have a New Request item to Fulfill", "", null );
                BrowserMessage returnMsg2 = emailService.sendSupervisorItemRequest(emailDetailsSupervisor, requestItem.get(), requestItem.get().getOrderRequest().getSupervisor().getId());

            }
        }
        return supervisorModel;
    }

    @PostMapping("/updateReason")
    public int emptyCart(@ModelAttribute BigIntegerStringModel requestModel, HttpSession session) {
        Optional<OrderRequest> request = orderService.findById(requestModel.id());
        if (request.isPresent()) {
            // update reason
            request.get().setRequestNote(requestModel.name());
            orderService.save(request.get());
            // add note
            orderNoteService.createOrderNote(new OrderNote(
                    null,
                    request.get().getId(),
                    null,
                    requestModel.name(),
                    BigInteger.valueOf(0),
                    request.get().getOrderStatus(),
                    request.get().getUser()));
            // email supervisor
            EmailDetails emailDetailsSupervisor =
                    new EmailDetails(request.get().getSupervisor().getContactEmail(),"A Request Reason has been Updated", "", null );
            BrowserMessage returnMsg2 = emailService.sendUserRequest(emailDetailsSupervisor, request.get());

            return 1;
        }
        session.setAttribute("msgError", "There was a problem updating your order!");
        return 0;
    }

    @PostMapping("/search")
    public List<UniversalSearchModel> searchRequests(@ModelAttribute SingleStringModel stringModel) {
        // init return
        List<UniversalSearchModel> searchReturn = new ArrayList<>();

        // determine if search for int
        Integer i = 0;
        try {
            i = Integer.parseInt(stringModel.name());
        } catch (NumberFormatException e) {
            // System.out.println(e);
        }
        // if searching order
        if (i>0) {
            BigInteger requestId = BigInteger.valueOf(i);
            List<OrderRequest> foundOrders = orderService.searchAllById(requestId);
            for (OrderRequest request : foundOrders) {
                searchReturn.add(new UniversalSearchModel(request.getId().toString(), "request", request.getId()));
            }
        } else {
            List<User> foundUsers = userService.searchAllByFullName(stringModel.name());
            for (User user : foundUsers) {
                String fullName = user.getFirstName()+" "+user.getLastName();
                searchReturn.add(new UniversalSearchModel(fullName, "user", user.getId()));
            }
        }
        return searchReturn;
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
