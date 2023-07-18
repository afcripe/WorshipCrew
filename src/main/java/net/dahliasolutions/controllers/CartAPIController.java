package net.dahliasolutions.controllers;

import lombok.RequiredArgsConstructor;
import net.dahliasolutions.models.*;
import net.dahliasolutions.services.CartItemService;
import net.dahliasolutions.services.CartService;
import net.dahliasolutions.services.OrderItemService;
import net.dahliasolutions.services.StoreItemService;
import org.springframework.web.bind.annotation.*;

import java.math.BigInteger;
import java.util.List;
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
        boolean itemFound = false;
        // loop through items and count them, update any found cart items
        for (CartItem item : cart.getCartItems()) {
            if (item.getProductId().equals(cartItemModel.id())) {
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
                        cartItemModel.count(),
                        storeItem.get().isSpecialOrder(),
                        storeItem.get().isAvailable(),
                        storeItem.get().getLeadTime(),
                        OrderStatus.cart,
                        storeItem.get().getOwner(),
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
        Cart cart = cartService.findById(integerModel.id());
        // loop through items and remove items
        for (CartItem item : cart.getCartItems()) {
            cartItemService.deleteById(item.getId());
        }

        List<CartItem> cartItemList = cartItemService.findByCart(cart);
        cart.getCartItems().clear();
        cart.setCartItems(cartItemList);
        cart = cartService.save(cart);

        return cart.getItemCount();
    }
}
