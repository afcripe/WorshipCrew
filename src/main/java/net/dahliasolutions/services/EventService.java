package net.dahliasolutions.services;

import lombok.RequiredArgsConstructor;
import net.dahliasolutions.models.BrowserMessage;
import net.dahliasolutions.models.Event;
import net.dahliasolutions.models.EventModule;
import net.dahliasolutions.models.Notification;
import net.dahliasolutions.models.mail.EmailDetails;
import net.dahliasolutions.models.order.OrderRequest;
import net.dahliasolutions.models.user.User;
import net.dahliasolutions.services.mail.EmailService;
import net.dahliasolutions.services.order.OrderService;
import net.dahliasolutions.services.user.UserService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class EventService {

    private final NotificationService notificationService;
    private final UserService userService;
    private final OrderService orderService;
    private final EmailService emailService;

    public void dispatchEvent(Event dispatch) {
        List<Notification> notifyList = notificationService.findAllByModuleAndType(dispatch.getModule(), dispatch.getType());
        for (Notification notify : notifyList) {
            switch (dispatch.getModule()) {
                case Request:
                    requestEvent(dispatch, notify);
                    break;
                default:
                    for (User u : notify.getUsers()) {
                        EmailDetails emailDetails =
                                new EmailDetails(u.getContactEmail(), dispatch.getName(), "", null);
                        BrowserMessage returnMsg = emailService.sendSystemNotification(emailDetails, dispatch);
                    }
            }

        }
    }

    private void requestEvent(Event dispatch, Notification notify) {
        switch (dispatch.getType()) {
            case New:OrderRequest request = orderService.findById(dispatch.getItemId()).get();
                for (User u : notify.getUsers()) {
                    EmailDetails emailDetails =
                            new EmailDetails(u.getContactEmail(), dispatch.getName(), "", null);
                    BrowserMessage returnMsg = emailService.sendUserRequest(emailDetails, request);
                }
                break;
            default:
                for (User u : notify.getUsers()) {
                    EmailDetails emailDetails =
                            new EmailDetails(u.getContactEmail(), dispatch.getName(), "", null);
                    BrowserMessage returnMsg = emailService.sendSystemNotification(emailDetails, dispatch);
                    System.out.println(returnMsg);
                }
        }
    }

}
