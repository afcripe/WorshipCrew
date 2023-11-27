package net.dahliasolutions.controllers.store;

import lombok.RequiredArgsConstructor;
import net.dahliasolutions.models.*;
import net.dahliasolutions.models.department.DepartmentRegional;
import net.dahliasolutions.models.mail.EmailDetails;
import net.dahliasolutions.models.order.OrderItem;
import net.dahliasolutions.models.order.OrderRequest;
import net.dahliasolutions.models.records.BigIntegerStringModel;
import net.dahliasolutions.models.records.SingleBigIntegerModel;
import net.dahliasolutions.models.records.SingleStringModel;
import net.dahliasolutions.models.store.*;
import net.dahliasolutions.models.user.User;
import net.dahliasolutions.services.EventService;
import net.dahliasolutions.services.department.DepartmentRegionalService;
import net.dahliasolutions.services.mail.EmailService;
import net.dahliasolutions.services.mail.NotificationMessageService;
import net.dahliasolutions.services.order.OrderService;
import net.dahliasolutions.services.store.CartItemService;
import net.dahliasolutions.services.store.CartService;
import net.dahliasolutions.services.store.StoreItemService;
import net.dahliasolutions.services.store.StoreSettingService;
import net.dahliasolutions.services.user.UserService;
import org.springframework.web.bind.annotation.*;

import java.math.BigInteger;
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
    private final EmailService emailService;
    private final NotificationMessageService messageService;
    private final StoreSettingService storeSettingService;
    private final EventService eventService;
    private final DepartmentRegionalService departmentRegionalService;

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
    public SingleBigIntegerModel placeOrder(@ModelAttribute BigIntegerStringModel cartModel) {
        Cart cart = cartService.findById(cartModel.id());
        Optional<User> user = userService.findById(cart.getId());
        User fulfillmentAgent = null;
        boolean splitOrder = false;

        String reasonForRequest = "None Given";
        if (cartModel.name() != null) {
            if (!cartModel.name().equals("")) {
                reasonForRequest = cartModel.name();
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
            case ItemDepartmentDirector:
                splitOrder = true;
                break;
            default:
                fulfillmentAgent = user.get().getDirector();
                break;
        }

        // creat list of orders to place
        List<OrderRequest> orderRequests = new ArrayList<>();

        // decide if we need to split up the order
        if (splitOrder) {
            // list departments
            List<DepartmentRegional> departmentRegionalList = departmentRegionalService.findAll();
            // find items for each department
            for (DepartmentRegional dep : departmentRegionalService.findAll()) {
                // create new order for each found department
                List<CartItem> depItems = cartItemService.findAllByCartAndAndDepartment(cart, dep);
                if (!depItems.isEmpty()) {
                    OrderRequest orderRequest = orderService.createOrder(cart);
                    orderRequest.setRequestNote(reasonForRequest);
                    orderRequest.setSupervisor(userService.findById(dep.getDirectorId()).orElse(null));
                    // add order items from depItems
                    for (CartItem item : depItems) {
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
                    // save request
                    orderRequest = orderService.save(orderRequest);
                    // add orderRequest to orders Array
                    orderRequests.add(orderRequest);
                }
            }
        } else {
            // create order for all items
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
            // save request
            orderRequest = orderService.save(orderRequest);
            // add orderRequest to orders Array
            orderRequests.add(orderRequest);
        }

        // loop through orders and send
        for (OrderRequest req : orderRequests) {
            // E-mail User
            EmailDetails emailDetailsUser =
                    new EmailDetails(BigInteger.valueOf(0), user.get().getContactEmail(), "Your Request", "", null);
            BrowserMessage returnMsg = emailService.sendUserRequest(emailDetailsUser, req);

            // Notify supervisor
            NotificationMessage returnMsg2 = messageService.createMessage(
                    new NotificationMessage(
                            null,
                            "A New Request",
                            req.getId().toString(),
                            BigInteger.valueOf(0),
                            null,
                            false,
                            false,
                            null,
                            false,
                            BigInteger.valueOf(0),
                            EventModule.Request,
                            EventType.New,
                            req.getSupervisor(),
                            BigInteger.valueOf(0)
                    ));

            // send any additional notifications
            AppEvent notifyEvent = eventService.createEvent(new AppEvent(
                    null,
                    "New Request",
                    "A new request has been placed by " + user.get().getFullName(),
                    req.getId().toString(),
                    EventModule.Request,
                    EventType.New,
                    new ArrayList<>()
            ));
        }

        // empty the cart
        cartService.emptyCart(cartModel.id());
        return new SingleBigIntegerModel(orderRequests.get(0).getId());
    }
}
