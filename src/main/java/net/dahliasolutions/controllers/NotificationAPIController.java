package net.dahliasolutions.controllers;

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
import net.dahliasolutions.services.NotificationService;
import net.dahliasolutions.services.mail.EmailService;
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
@RequestMapping("/api/v1/notification")
@RequiredArgsConstructor
public class NotificationAPIController {

    private final OrderService orderService;
    private final OrderItemService orderItemService;
    private final OrderNoteService orderNoteService;
    private final UserService userService;
    private final UserRolesService rolesService;
    private final EmailService emailService;
    private final EventService eventService;
    private final NotificationService notificationService;

    @PostMapping("/newnotification")
    public BigIntegerStringModel updateOrderNotification(@ModelAttribute BigIntegerStringModel notifyModel) {
        Notification notify = new Notification(
                null, notifyModel.name(), "",
                EventModule.Request, EventType.New, new ArrayList<>());
        notify = notificationService.save(notify);
        return new BigIntegerStringModel(notify.getId(), notify.getName());
    }

    @PostMapping("/getnotification")
    public Notification getOrderNotification(@ModelAttribute SingleBigIntegerModel intModel) {
        Optional<Notification> notify = notificationService.findById(intModel.id());
        return notify.get();
    }

    @PostMapping("/updatenotification")
    public NotificationModel updateOrderNotification(@ModelAttribute NotificationModel notifyModel) {
        System.out.println(notifyModel);
        Optional<Notification> notify = notificationService.findById(notifyModel.id());

        if (notify.isPresent()) {
            notify.get().setName(notifyModel.name());
            notify.get().setDescription(notifyModel.description());
            notify.get().setModule(EventModule.valueOf(notifyModel.module()));
            notify.get().setType(EventType.valueOf(notifyModel.type()));

            // update user list
            List<String> items = Arrays.asList(notifyModel.users().split("\s"));
            ArrayList<User> ul = new ArrayList<>();
            for (String s : items) {
                if (!s.equals("")) {
                    try {
                        int i = Integer.parseInt(s);
                        Optional<User> u = userService.findById(BigInteger.valueOf(i));
                        if (u.isPresent()) {
                            ul.add(u.get());
                        }
                    } catch (Error e) {
                        System.out.println(e);
                    }
                }
            }
            notify.get().setUsers(ul);

            notificationService.save(notify.get());
        }
        return notifyModel;
    }

    @PostMapping("/deletenotification")
    public SingleBigIntegerModel deleteOrderNotification(@ModelAttribute SingleBigIntegerModel intModel) {
        Optional<Notification> notify = notificationService.findById(intModel.id());
        notify.ifPresent(notification -> notificationService.deleteById(notification.getId()));
        return intModel;
    }

}
