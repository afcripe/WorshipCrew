package net.dahliasolutions.controllers.order;

import lombok.RequiredArgsConstructor;
import net.dahliasolutions.models.AddSupervisorModel;
import net.dahliasolutions.models.ChangeStatusModel;
import net.dahliasolutions.models.SingleBigIntegerModel;
import net.dahliasolutions.models.order.OrderNote;
import net.dahliasolutions.models.order.OrderRequest;
import net.dahliasolutions.models.order.OrderStatus;
import net.dahliasolutions.models.user.User;
import net.dahliasolutions.models.user.UserRoles;
import net.dahliasolutions.services.order.OrderNoteService;
import net.dahliasolutions.services.order.OrderService;
import net.dahliasolutions.services.user.UserRolesService;
import net.dahliasolutions.services.user.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/v1/order")
@RequiredArgsConstructor
public class OrderAPIController {

    private final OrderService orderService;
    private final OrderNoteService orderNoteService;
    private final UserService userService;
    private final UserRolesService rolesService;

    @GetMapping("")
    public String getOrders(){ return null; }

    @PostMapping("/cancel")
    public SingleBigIntegerModel cancelOrder(@ModelAttribute SingleBigIntegerModel integerModel) {
        Optional<OrderRequest> orderRequest = orderService.findById(integerModel.id());
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) auth.getPrincipal();

        if (orderRequest.isPresent()) {
            if (!orderRequest.get().getOrderStatus().equals("Cancelled") || !orderRequest.get().getOrderStatus().equals("Complete")) {
                OrderNote orderNote = orderNoteService.createOrderNote(new OrderNote(
                        null,
                        orderRequest.get().getId(),
                        null,
                        "User Cancelled Order",
                        OrderStatus.Cancelled,
                        user));
                orderRequest.get().setOrderStatus(orderNote.getOrderStatus());
                orderRequest.get().setRequestNote(orderNote.getOrderNote());
                orderService.save(orderRequest.get());
            }
        }
        System.out.println(integerModel.id());
        return integerModel;
    }

    @GetMapping("/getstatusoptions")
    public List<OrderStatus> getStatusOptions(){
        List<OrderStatus> statuses = new ArrayList<OrderStatus>(Arrays.asList(OrderStatus.values()));
        return new ArrayList<OrderStatus>(Arrays.asList(OrderStatus.values()));
    }

    @GetMapping("/getupervisors")
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
            orderRequest.get().setRequestNote(statusModel.RequestNote());
            orderService.save(orderRequest.get());
            orderNoteService.createOrderNote(new OrderNote(
                    null,
                    orderRequest.get().getId(),
                    null,
                    statusModel.RequestNote(),
                    OrderStatus.valueOf(statusModel.requestStatus()),
                    user));
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
                    noteDetail = newSuper.get().getFirstName()+" "+newSuper.get().getLastName()+" was set as supervisor of the order.";

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
                            orderRequest.get().getOrderStatus(),
                            user));
                } else {
                    noteDetail = newSuper.get().getFirstName()+" "+newSuper.get().getLastName()+" was add to the order.";

                    orderRequest.get().setSupervisorList(
                            addToSupervisorList(orderRequest.get().getSupervisor(), orderRequest.get().getSupervisorList()));

                    orderService.save(orderRequest.get());
                    orderNoteService.createOrderNote(new OrderNote(
                            null,
                            orderRequest.get().getId(),
                            null,
                            noteDetail,
                            orderRequest.get().getOrderStatus(),
                            user));
                }
            }
        }
            return supervisorModel;
    }



    private Collection<UserRoles> getSupervisorCollection() {
        Collection<UserRoles> roles = new ArrayList<>();
        roles.add(rolesService.findByName("ADMIN_WRITE").get());
        roles.add(rolesService.findByName("DIRECTOR_READ").get());
        roles.add(rolesService.findByName("CAMPUS_WRITE").get());
        roles.add(rolesService.findByName("REQUEST_WRITE").get());
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
