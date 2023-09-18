package net.dahliasolutions.controllers.order;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import net.dahliasolutions.models.*;
import net.dahliasolutions.models.mail.EmailDetails;
import net.dahliasolutions.models.order.OrderItem;
import net.dahliasolutions.models.order.OrderNote;
import net.dahliasolutions.models.order.OrderRequest;
import net.dahliasolutions.models.order.OrderStatus;
import net.dahliasolutions.models.records.*;
import net.dahliasolutions.models.user.User;
import net.dahliasolutions.models.user.UserRoles;
import net.dahliasolutions.services.EventService;
import net.dahliasolutions.services.mail.EmailService;
import net.dahliasolutions.services.mail.NotificationMessageService;
import net.dahliasolutions.services.order.OrderItemService;
import net.dahliasolutions.services.order.OrderNoteService;
import net.dahliasolutions.services.order.OrderService;
import net.dahliasolutions.services.user.UserRolesService;
import net.dahliasolutions.services.user.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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
    private final NotificationMessageService messageService;
    private final EventService eventService;

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

                // Notify supervisor
                NotificationMessage returnMsg2 = messageService.createMessage(
                        new NotificationMessage(
                                null,
                                "A Request Has Been Cancelled",
                                orderRequest.get().getId().toString(),
                                BigInteger.valueOf(0),
                                false,
                                false,
                                null,
                                EventModule.Request,
                                NotificationType.Updated,
                                orderRequest.get().getSupervisor(),
                                BigInteger.valueOf(0)

                        ));

//                EmailDetails emailDetailsSupervisor =
//                        new EmailDetails(orderRequest.get().getSupervisor().getContactEmail(),"A Request Has Been Cancelled", "", null );
//                BrowserMessage returnMsg2 = emailService.sendUserRequest(emailDetailsSupervisor, orderRequest.get());

                // send any additional notifications
                String userFullName = orderRequest.get().getUser().getFirstName()+" "+orderRequest.get().getUser().getLastName();
                String eventName = "A Request Has Been Cancelled.";
                String eventDesc = "Request "+orderRequest.get().getId().toString()+" has been cancelled by "+userFullName;
                    // cancel
                Event e = new Event(null, eventName, eventDesc, orderRequest.get().getId(), "", EventModule.Request, EventType.Cancelled);
                eventService.dispatchEvent(e);
            }
        }

        return integerModel;
    }

    @GetMapping("/getstatusoptions")
    public List<OrderStatus> getStatusOptions(){
        return Arrays.asList(OrderStatus.values());
    }

    @GetMapping("/getsupervisors")
    public List<User> getSupervisors(){
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
        return userList;
    }

    @PostMapping("/changestatus")
    public ChangeStatusModel updateRequestStatus(@ModelAttribute ChangeStatusModel statusModel) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) auth.getPrincipal();
        OrderStatus setStatus = OrderStatus.valueOf(statusModel.requestStatus());
        Optional<OrderRequest> orderRequest = orderService.findById(statusModel.requestId());
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
                    user));
        }

        EmailDetails emailDetailsUser =
                new EmailDetails(user.getContactEmail(),"Your Request Has Been Updated", "", null );
        BrowserMessage returnMsg = emailService.sendUserRequest(emailDetailsUser, orderRequest.get());

        // send any additional notifications
        String userFullName = user.getFirstName()+" "+user.getLastName();
        String eventName = "Request "+orderRequest.get().getId()+" Status has been updated to "+setStatus;
        String eventDesc = "The Status for Request "+orderRequest.get().getId()+
                " has been updated to ["+setStatus+"] by "+userFullName;
        Event e = new Event(null, eventName, eventDesc, orderRequest.get().getId(), "", EventModule.Request, EventType.Changed);
        eventService.dispatchEvent(e);
        //closed
        if (setStatus.equals(OrderStatus.Complete)) {
            e.setType(EventType.Closed);
            eventService.dispatchEvent(e);
        }

        return statusModel;
    }

    @PostMapping("/changeitemstatus")
    public ChangeStatusModel updateItemStatus(@ModelAttribute ChangeStatusModel statusModel) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) auth.getPrincipal();
        OrderStatus setStatus = OrderStatus.valueOf(statusModel.requestStatus());
        Optional<OrderItem> requestItem = orderItemService.findById(statusModel.requestId());
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
                    user));
            // Email everyone
            EmailDetails emailDetailsUser =
                    new EmailDetails(requestItem.get().getOrderRequest().getUser().getContactEmail(),"The Status of a Request Item Changed", "", null );
            BrowserMessage returnMsg = emailService.sendItemUpdate(emailDetailsUser, requestItem.get(), orderNote);

            // Notify supervisor
            NotificationMessage returnMsg2 = messageService.createMessage(
                    new NotificationMessage(
                            null,
                            "The Status of a Request Item Changed",
                            "",
                            requestItem.get().getId(),
                            false,
                            false,
                            null,
                            EventModule.Request,
                            NotificationType.ItemUpdated,
                            requestItem.get().getOrderRequest().getSupervisor(),
                            orderNote.getId()

                    ));

//            EmailDetails emailDetailsSupervisor =
//                    new EmailDetails(requestItem.get().getOrderRequest().getSupervisor().getContactEmail(),"The Status of a Request Item Changed", "", null );
//            BrowserMessage returnMsg2 = emailService.sendItemUpdate(emailDetailsSupervisor, requestItem.get(), orderNote);


            // send any additional notifications
            String userFullName = user.getFirstName()+" "+user.getLastName();
            String eventName = "A Requested Item Status was changed to "+setStatus;
            String eventDesc = "An Item Status for Request ID "+requestItem.get().getOrderRequest().getId()+
                    " has been updated to ["+setStatus+"] by "+userFullName;
            Event e = new Event(null, eventName, eventName, requestItem.get().getOrderRequest().getId(), "", EventModule.Request, EventType.ItemUpdated);
            eventService.dispatchEvent(e);
            // complete
            if (setStatus.equals(OrderStatus.Complete)) {
                e.setType(EventType.Closed);
                eventService.dispatchEvent(e);
            }
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

                    // send any additional notifications
                    String userFullName = user.getFirstName()+" "+user.getLastName();
                    String eventName = "Request "+orderRequest.get().getId()+" Supervisor was changed to "+newSuper.get().getFirstName()+" "+newSuper.get().getLastName();
                    String eventDesc = "Request ID "+orderRequest.get().getId()+" was assigned to "+
                            newSuper.get().getFirstName()+" "+newSuper.get().getLastName()+" by "+userFullName;
                    Event e = new Event(null, eventName, eventDesc, orderRequest.get().getId(), "", EventModule.Request, EventType.Changed);
                    eventService.dispatchEvent(e);
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
                            user));

                    // send any additional notifications
                    String userFullName = user.getFirstName()+" "+user.getLastName();
                    String eventName = newSuper.get().getFirstName()+" "+newSuper.get().getLastName()+" was added to a Request "+orderRequest.get().getId();
                    String eventDesc = newSuper.get().getFirstName()+" "+newSuper.get().getLastName()+
                            " has been added to Request "+orderRequest.get().getId()+" by "+userFullName;
                    Event e = new Event(null, eventName, eventDesc, orderRequest.get().getId(), "", EventModule.Request, EventType.Changed);
                    eventService.dispatchEvent(e);
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

                // send any additional notifications
                String userFullName = user.getFirstName()+" "+user.getLastName();
                String eventName = newSuper.get().getFirstName()+" "+newSuper.get().getLastName()+" was removed from Request "+orderRequest.get().getId();
                String eventDesc = newSuper.get().getFirstName()+" "+newSuper.get().getLastName()+
                        " was removed from Request "+orderRequest.get().getId()+" by "+userFullName;
                Event e = new Event(null, eventName, eventDesc, orderRequest.get().getId(), "", EventModule.Request, EventType.Changed);
                eventService.dispatchEvent(e);
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
                        user));

                // Notify supervisor
                NotificationMessage returnMsg2 = messageService.createMessage(
                        new NotificationMessage(
                                null,
                                "You have a New Request item to Fulfill",
                                "",
                                requestItem.get().getId(),
                                false,
                                false,
                                null,
                                EventModule.Request,
                                NotificationType.NewItem,
                                newSuper.get(),
                                BigInteger.valueOf(0)

                        ));

                // email new supervisor
//                EmailDetails emailDetailsSupervisor =
//                        new EmailDetails(newSuper.get().getContactEmail(),"You have a New Request item to Fulfill", "", null );
//                BrowserMessage returnMsg2 = emailService.sendSupervisorItemRequest(emailDetailsSupervisor, requestItem.get(), requestItem.get().getOrderRequest().getSupervisor().getId());

                // send any additional notifications
                String userFullName = user.getFirstName()+" "+user.getLastName();
                String eventName = newSuper.get().getFirstName()+" "+newSuper.get().getLastName()+" has been made a Supervisor on an item for request "+requestItem.get().getOrderRequest().getId();
                String eventDesc = "An item in Request "+requestItem.get().getOrderRequest().getId()+" was assigned to "+
                        newSuper.get().getFirstName()+" "+newSuper.get().getLastName()+
                        " by "+userFullName;
                Event e = new Event(null, eventName, eventDesc, requestItem.get().getOrderRequest().getId(), "", EventModule.Request, EventType.Changed);
                eventService.dispatchEvent(e);
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

            // Notify supervisor
            NotificationMessage returnMsg2 = messageService.createMessage(
                    new NotificationMessage(
                            null,
                            "A Request Reason has been Updated",
                            request.get().getId().toString(),
                            BigInteger.valueOf(0),
                            false,
                            false,
                            null,
                            EventModule.Request,
                            NotificationType.Updated,
                            request.get().getSupervisor(),
                            BigInteger.valueOf(0)

                    ));
//            EmailDetails emailDetailsSupervisor =
//                    new EmailDetails(request.get().getSupervisor().getContactEmail(),"A Request Reason has been Updated", "", null );
//            BrowserMessage returnMsg2 = emailService.sendUserRequest(emailDetailsSupervisor, request.get());

            // send any additional notifications
            String userFullName = request.get().getUser()+" "+request.get().getUser();
            String eventName = "A Request Reason was updated by "+userFullName;
            String eventDesc = "A Request Reason for Request with ID, "+request.get().getId()+
                    ", was update to ("+requestModel.name()+") by "+userFullName;
            Event e = new Event(null, eventName, eventDesc, request.get().getId(), "", EventModule.Request, EventType.Changed);
            eventService.dispatchEvent(e);

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
                searchReturn.add(new UniversalSearchModel(request.getId().toString(), "request", request.getId(), ""));
            }
        } else {
            List<User> foundUsers = userService.searchAllByFullName(stringModel.name());
            for (User user : foundUsers) {
                searchReturn.add(new UniversalSearchModel(user.getFullName(), "user", user.getId(), ""));
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
