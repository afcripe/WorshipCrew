package net.dahliasolutions.controllers.store;

import lombok.RequiredArgsConstructor;
import net.dahliasolutions.models.*;
import net.dahliasolutions.models.mail.EmailDetails;
import net.dahliasolutions.models.order.OrderItem;
import net.dahliasolutions.models.order.OrderRequest;
import net.dahliasolutions.models.records.SingleBigIntegerModel;
import net.dahliasolutions.models.records.SingleStringModel;
import net.dahliasolutions.models.store.*;
import net.dahliasolutions.models.user.User;
import net.dahliasolutions.services.mail.EmailService;
import net.dahliasolutions.services.order.OrderService;
import net.dahliasolutions.services.store.CartItemService;
import net.dahliasolutions.services.store.CartService;
import net.dahliasolutions.services.store.StoreItemService;
import net.dahliasolutions.services.store.StoreSettingService;
import net.dahliasolutions.services.user.UserService;
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
    private final OrderService orderService;
    private final UserService userService;
    private final EmailService emailService;
    private final StoreSettingService storeSettingService;

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
                        storeItem.get().getDepartment(),
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
    public SingleBigIntegerModel placeOrder(@ModelAttribute SingleBigIntegerModel integerModel,
                                            @ModelAttribute SingleStringModel stringModel) {
        Cart cart = cartService.findById(integerModel.id());
        Optional<User> user = userService.findById(cart.getId());
        User fulfillmentAgent;

        String reasonForRequest = "None Given";
        if (stringModel.name() != null) {
            if (!stringModel.name().equals("")) {
                reasonForRequest = stringModel.name();
            }
        }

        // determine who gets the fulfillment request
        StoreSetting storeSetting = storeSettingService.getStoreSetting();
        switch (storeSetting.getNotifyTarget()) {
            case User:
                if (storeSetting.getUser() != null) {
                    fulfillmentAgent = storeSetting.getUser();
                    break;
                }
            case RegionalDepartmentDirector:
                fulfillmentAgent = userService.findById(user.get().getDepartment().getRegionalDepartment().getDirectorId()).get();
                break;
            case CampusDepartmentDirector:
                fulfillmentAgent = userService.findById(user.get().getDepartment().getDirectorId()).get();
                break;
            case CampusDirector:
                fulfillmentAgent = userService.findById(user.get().getCampus().getDirectorId()).get();
                break;
            default:
                fulfillmentAgent = user.get().getDirector();
                break;
        }

        // create order
        OrderRequest orderRequest = orderService.createOrder(cart);
        orderRequest.setRequestNote(reasonForRequest);
        orderRequest.setSupervisor(fulfillmentAgent);

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
                        orderRequest.getRequestDate(),
                        item.getDepartment(),
                        orderRequest.getOrderStatus(),
                        orderRequest.getSupervisor(),
                        item.getImage(),
                        orderRequest
                );
            orderRequest.getRequestItems().add(orderItem);
        }
        orderService.save(orderRequest);

        EmailDetails emailDetailsUser =
                new EmailDetails(user.get().getContactEmail(),"Your Request", "", null );
        BrowserMessage returnMsg = emailService.sendUserRequest(emailDetailsUser, orderRequest);

        EmailDetails emailDetailsSupervisor =
                new EmailDetails(orderRequest.getSupervisor().getContactEmail(),"A New Request", "", null );
        BrowserMessage returnMsg2 = emailService.sendSupervisorRequest(emailDetailsSupervisor, orderRequest, orderRequest.getSupervisor().getId());

        // send any additional notifications

        cartService.emptyCart(integerModel.id());
        return new SingleBigIntegerModel(orderRequest.getId());
    }
}
