package net.dahliasolutions.controllers.order;

import lombok.RequiredArgsConstructor;
import net.dahliasolutions.models.SingleBigIntegerModel;
import net.dahliasolutions.models.order.OrderNote;
import net.dahliasolutions.models.order.OrderRequest;
import net.dahliasolutions.models.order.OrderStatus;
import net.dahliasolutions.models.user.User;
import net.dahliasolutions.services.order.OrderNoteService;
import net.dahliasolutions.services.order.OrderService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/v1/order")
@RequiredArgsConstructor
public class OrderAPIController {

    private final OrderService orderService;
    private final OrderNoteService orderNoteService;

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
}
