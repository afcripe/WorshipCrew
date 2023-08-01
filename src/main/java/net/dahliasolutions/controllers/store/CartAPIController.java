package net.dahliasolutions.controllers.store;

import lombok.RequiredArgsConstructor;
import net.dahliasolutions.models.*;
import net.dahliasolutions.models.order.OrderItem;
import net.dahliasolutions.models.order.OrderRequest;
import net.dahliasolutions.models.order.OrderStatus;
import net.dahliasolutions.models.store.*;
import net.dahliasolutions.models.user.User;
import net.dahliasolutions.services.order.OrderService;
import net.dahliasolutions.services.store.CartItemService;
import net.dahliasolutions.services.store.CartService;
import net.dahliasolutions.services.store.StoreItemService;
import net.dahliasolutions.services.user.UserService;
import org.springframework.web.bind.annotation.*;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/cart")
@RequiredArgsConstructor
public class CartAPIController {

    private final CartService cartService;
    private final CartItemService cartItemService;
    private final StoreItemService storeItemService;
    private final OrderService orderService;
    private final UserService userService;

    @GetMapping("{id}")
    public Cart getCart(@PathVariable BigInteger id) {
        return cartService.findById(id);
    }

    @PostMapping("/additem")
    public int addItemToCart(@ModelAttribute CartItemModel cartItemModel) {
        Cart cart = cartService.findById(cartItemModel.userId());
        boolean itemFound = false;
        // loop through items and count them, update any found cart items
        for (CartItem item : cart.getCartItems()) {
            if (item.getProductId().equals(cartItemModel.id()) && item.getDetails().equals(cartItemModel.details())) {
                itemFound = true;
                item.setCount(item.getCount()+cartItemModel.count());
                cartItemService.save(item);
            }
        }
        // if no existing cart item found, add it
        if (!itemFound) {
            Optional<StoreItem> storeItem = storeItemService.findById(cartItemModel.id());
            if (storeItem.isPresent()) {
                CartItem newItem = new CartItem(
                        null,
                        storeItem.get().getId(),
                        storeItem.get().getName(),
                        cartItemModel.details(),
                        cartItemModel.count(),
                        storeItem.get().isSpecialOrder(),
                        storeItem.get().isAvailable(),
                        storeItem.get().getLeadTime(),
                        storeItem.get().getImage(),
                        cart
                );
                cart.getCartItems().add(newItem);
            }
        }

        cart = cartService.save(cart);

        return cart.getItemCount();
    }

    @PostMapping("/updateitem")
    public int updateItemInCart(@ModelAttribute CartItemModel cartItemModel) {
        Cart cart = cartService.findById(cartItemModel.userId());
        // loop through items and count them, update any found cart items
        for (CartItem item : cart.getCartItems()) {
            if (item.getProductId().equals(cartItemModel.id())) {
                if (cartItemModel.count() == 0) {
                    cartItemService.deleteById(item.getId());
                } else {
                    item.setCount(cartItemModel.count());
                    cartItemService.save(item);
                }
            }
        }

        List<CartItem> cartItemList = cartItemService.findByCart(cart);
        cart.getCartItems().clear();
        cart.setCartItems(cartItemList);
        cart = cartService.save(cart);

        return cart.getItemCount();
    }

    @PostMapping("/removeitem")
    public int removeItemFromCartAll(@ModelAttribute CartItemModel cartItemModel) {
        Cart cart = cartService.findById(cartItemModel.userId());
        // loop through items and count them, update any found cart items
        for (CartItem item : cart.getCartItems()) {
            if (item.getProductId().equals(cartItemModel.id())) {
                cartItemService.deleteById(item.getId());
            }
        }

        List<CartItem> cartItemList = cartItemService.findByCart(cart);
        cart.getCartItems().clear();
        cart.setCartItems(cartItemList);
        cart = cartService.save(cart);

        return cart.getItemCount();
    }


    @PostMapping("/empty")
    public int emptyCart(@ModelAttribute SingleBigIntegerModel integerModel) {
        return cartService.emptyCart(integerModel.id()).getItemCount();
    }

    @PostMapping("/placeOrder")
    public SingleBigIntegerModel placeOrder(@ModelAttribute SingleBigIntegerModel integerModel) {
        Cart cart = cartService.findById(integerModel.id());
        OrderRequest orderRequest = orderService.createOrder(cart);

        for (CartItem item : cart.getCartItems()) {
                OrderItem orderItem = new OrderItem(
                        null,
                        item.getProductId(),
                        item.getProductName(),
                        item.getDetails(),
                        item.getCount(),
                        item.isSpecialOrder(),
                        item.isAvailable(),
                        item.getLeadTime(),
                        item.getImage(),
                        orderRequest
                );
            orderRequest.getRequestItems().add(orderItem);
        }
        orderService.save(orderRequest);

        cartService.emptyCart(integerModel.id());
        return new SingleBigIntegerModel(orderRequest.getId());
    }
}
