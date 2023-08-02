package net.dahliasolutions.controllers.store;

import lombok.RequiredArgsConstructor;
import net.dahliasolutions.models.order.OrderRequest;
import net.dahliasolutions.models.store.Cart;
import net.dahliasolutions.models.user.User;
import net.dahliasolutions.services.order.OrderService;
import net.dahliasolutions.services.store.CartService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;
    private final OrderService orderService;

    @ModelAttribute
    public void addAttributes(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) auth.getPrincipal();
        model.addAttribute("moduleTitle", "Cart");
        model.addAttribute("moduleLink", "/cart");
        model.addAttribute("userId", user.getId());
    }

    @GetMapping("")
    public String getCart(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) auth.getPrincipal();
        Cart cart = cartService.findById(user.getId());
        List<OrderRequest> orderList = orderService.findFirst5ByUser(user);

        model.addAttribute("cart", cart);
        model.addAttribute("orderList", orderList);
        return "cart";
    }

}
