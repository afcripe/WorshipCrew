package net.dahliasolutions.controllers.order;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import net.dahliasolutions.models.SingleStringModel;
import net.dahliasolutions.models.UniversalSearchModel;
import net.dahliasolutions.models.department.DepartmentRegional;
import net.dahliasolutions.models.order.OrderItem;
import net.dahliasolutions.models.order.OrderNote;
import net.dahliasolutions.models.order.OrderRequest;
import net.dahliasolutions.models.user.User;
import net.dahliasolutions.models.user.UserRoles;
import net.dahliasolutions.services.RedirectService;
import net.dahliasolutions.services.mail.EmailService;
import net.dahliasolutions.services.order.OrderService;
import net.dahliasolutions.services.order.OrderNoteService;
import net.dahliasolutions.services.order.OrderItemService;
import net.dahliasolutions.services.user.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/order")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final OrderItemService orderItemService;
    private final OrderNoteService orderNoteService;
    private final UserService userService;
    private final RedirectService redirectService;

    @ModelAttribute
    public void addAttributes(Model model) {
        model.addAttribute("moduleTitle", "Requests");
        model.addAttribute("moduleLink", "/order");
    }

    @GetMapping("")
    public String getSupervisorOrders(Model model, HttpSession session) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) auth.getPrincipal();

        List<OrderItem> openItemList = orderItemService.findAllBySupervisorOpenOnly(user);
        List<OrderRequest> openOrderList = orderService.findAllBySupervisorOpenOnly(user);
        List<OrderRequest> orderMentionList = orderService.findAllByMentionOpenOnly(user);

        model.addAttribute("editable", false);
        model.addAttribute("openItemList", openItemList);
        model.addAttribute("openOrderList", openOrderList);
        model.addAttribute("orderMentionList", orderMentionList);
        redirectService.setHistory(session, "/order");
        return "order/orderList";
    }

    @GetMapping("/user/{id}")
    public String getUserOrders(@PathVariable BigInteger id, Model model, HttpSession session) {
        Optional<User> user = userService.findById(id);

        if (user.isEmpty()) {
            session.setAttribute("msgError", "User not found.");
            return redirectService.pathName(session, "/order");
        }

        List<OrderRequest> orderList = orderService.findAllByUser(user.get());
        List<OrderItem> openItemList = orderItemService.findAllBySupervisorOpenOnly(user.get());
        List<OrderRequest> openOrderList = orderService.findAllBySupervisorOpenOnly(user.get());
        List<OrderRequest> orderMentionList = orderService.findAllByMentionOpenOnly(user.get());

        model.addAttribute("editable", false);
        model.addAttribute("searchedUser", user.get().getFirstName()+" "+user.get().getLastName());
        model.addAttribute("orderList", orderList);
        model.addAttribute("openItemList", openItemList);
        model.addAttribute("openOrderList", openOrderList);
        model.addAttribute("orderMentionList", orderMentionList);
        redirectService.setHistory(session, "/order/user"+id);
        return "order/orderUser";
    }

    @GetMapping("/{id}")
    public String getOrder(@PathVariable BigInteger id, Model model, HttpSession session) {

        Optional<OrderRequest> request = orderService.findById(id);
        if (request.isEmpty()) {
            session.setAttribute("msgError", "Request not found.");
            return redirectService.pathName(session, "/order");
        }
        // get Edit permission
        boolean editable = false;
        if (allowEdit(request.get())) {
            editable = true;
        }
        List<OrderNote> noteList = orderNoteService.findByOrderId(id);

        model.addAttribute("editable", editable);
        model.addAttribute("orderRequest", request.get());
        model.addAttribute("noteList", noteList);
        redirectService.setHistory(session, "/order"+request.get().getId());
        return "order/order";
    }

    @PostMapping("/search")
    public String searchRequests(@ModelAttribute UniversalSearchModel searchModel, HttpSession session) {
        // determine if search type
        switch (searchModel.getSearchType()) {
            case "request":
                return "redirect:/order/"+searchModel.getSearchId();
            case "user":
                return "redirect:/order/user/"+searchModel.getSearchId();
            default:
                return "redirect:/order";
        }
    }

    /*  Determine Edit Permissions */
    private boolean allowEdit(OrderRequest request){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) auth.getPrincipal();
        Collection<UserRoles> roles = user.getUserRoles();

        for (UserRoles role : roles){
            if (role.getName().equals("ADMIN_WRITE") || role.getName().equals("REQUEST_SUPERVISOR")
                    || role.getName().equals("REQUEST_WRITE")) {
                return true;
            }
        }
        if (request.getSupervisor().equals(user)) {
            return true;
        }
        for (OrderItem item : request.getRequestItems()) {
            if (item.getSupervisor().equals(user)) {
                return true;
            }
        }
        return false;
    }

}
