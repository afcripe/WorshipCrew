package net.dahliasolutions.controllers;

import lombok.RequiredArgsConstructor;
import net.dahliasolutions.models.*;
import net.dahliasolutions.services.CartItemService;
import net.dahliasolutions.services.CartService;
import net.dahliasolutions.services.OrderItemService;
import net.dahliasolutions.services.StoreItemService;
import org.springframework.web.bind.annotation.*;

import java.math.BigInteger;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/cart")
@RequiredArgsConstructor
public class CartAPIController {

    private final CartService cartService;
    private final CartItemService cartItemService;
    private final StoreItemService storeItemService;

    @GetMapping("{id}")
    public Cart getCart(@PathVariable BigInteger id) {
        return cartService.findById(id);
    }

    @PostMapping("/additem")
    public int addItemToCart(@ModelAttribute CartItemModel cartItemModel) {
        Cart cart = cartService.findById(cartItemModel.userId());
        int totalItems = 0;
        boolean itemFound = false;
        // loop through items and count them, update any found cart items
        for (CartItem item : cart.getCartItems()) {
            if (item.getProductId().equals(cartItemModel.id())) {
                itemFound = true;
                item.setCount(item.getCount()+cartItemModel.count());
                cartItemService.save(item);
            }
            totalItems = totalItems+item.getCount();
        }
        // if no existing cart item found, add it
        if (!itemFound) {
            Optional<StoreItem> storeItem = storeItemService.findById(cartItemModel.id());
            if (storeItem.isPresent()) {
                CartItem newItem = new CartItem(
                    null,
                    storeItem.get().getId(),
                    storeItem.get().getName(),
                    cartItemModel.count(),
                    storeItem.get().isSpecialOrder(),
                    storeItem.get().isAvailable(),
                    storeItem.get().getLeadTime(),
                    OrderStatus.cart,
                    storeItem.get().getOwner(),
                    storeItem.get().getImage()
                );
                newItem = cartItemService.createCartItem(newItem);
                cart.getCartItems().add(newItem);
            }
            totalItems = totalItems+cartItemModel.count();
        }

        cart.setItemCount(totalItems);
        cartService.save(cart);

        return totalItems;
    }

    @PostMapping("/removeitem")
    public int removeItemFromCart(@ModelAttribute CartItemModel cartItemModel) {
        Cart cart = cartService.findById(cartItemModel.userId());
        int totalItems = 0;
        // loop through items and count them, update any found cart items
        for (CartItem item : cart.getCartItems()) {
            if (item.getProductId().equals(cartItemModel.id())) {
                if (item.getCount() > cartItemModel.count()) {
                    item.setCount(item.getCount()-cartItemModel.count());
                    cartItemService.save(item);
                    totalItems = totalItems + item.getCount();
                } else {
                    cartItemService.deleteById(item.getId());
                }
            } else {
                totalItems = totalItems + item.getCount();
            }
        }

        return totalItems;
    }

    @PostMapping("/removeitemall")
    public int removeItemFromCartAll(@ModelAttribute CartItemModel cartItemModel) {
        Cart cart = cartService.findById(cartItemModel.userId());
        int totalItems = 0;
        // loop through items and count them, update any found cart items
        for (CartItem item : cart.getCartItems()) {
            if (item.getProductId().equals(cartItemModel.id())) {
                cartItemService.deleteById(item.getId());
            } else {
                totalItems = totalItems + item.getCount();
            }
        }

        return totalItems;
    }


    @PostMapping("/empty")
    public int emptyCart(@ModelAttribute CartItemModel cartItemModel) {
        Cart cart = cartService.findById(cartItemModel.userId());
        // loop through items and remove items
        for (CartItem item : cart.getCartItems()) {
            cartItemService.deleteById(item.getId());
        }

        return 0;
    }
}
