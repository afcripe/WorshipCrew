package net.dahliasolutions.services;

import lombok.RequiredArgsConstructor;
import net.dahliasolutions.models.*;
import net.dahliasolutions.models.mail.EmailDetails;
import net.dahliasolutions.models.order.OrderRequest;
import net.dahliasolutions.models.user.User;
import net.dahliasolutions.services.mail.EmailService;
import net.dahliasolutions.services.order.OrderService;
import net.dahliasolutions.services.user.UserService;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class EventService {

    private final NotificationService notificationService;
    private final MessageService messageService;
    private final UserService userService;
    private final OrderService orderService;
    private final EmailService emailService;

    public Event dispatchEvent(Event dispatch) {
        List<Notification> notifyList = notificationService.findAllByModuleAndType(dispatch.getModule(), dispatch.getType());
        Event newEvent = dispatch;
        Message message;

        // if no notifications found, return
        if (notifyList.isEmpty()) {
            return newEvent;
        }

        // create new message from notification with event id
        if (dispatch.getId() == null) {
            newEvent.setId(Instant.now().toEpochMilli());
            message = messageService.createMessage(newEvent.getId(), notifyList.get(0));
        } else {
            message = messageService.findById(newEvent.getId()).get();
        }
        for (Notification notify : notifyList) {
            switch (dispatch.getModule()) {
                case Request:
                    requestEvent(dispatch, notify, message);
                    break;
                default:
                    for (User u : notify.getUsers()) {
                        if (!message.getUsers().contains(u)) {
                            EmailDetails emailDetails =
                                    new EmailDetails(u.getContactEmail(), dispatch.getName(), "", null);
                            BrowserMessage returnMsg = emailService.sendSystemNotification(emailDetails, dispatch);
                            message.getUsers().add(u);
                        }
                    }
                    messageService.save(message);
            }

        }
        return newEvent;
    }

    private void requestEvent(Event dispatch, Notification notify, Message message) {
        switch (dispatch.getType()) {
            case New:OrderRequest request = orderService.findById(dispatch.getItemId()).get();
                for (User u : notify.getUsers()) {
                    if (!message.getUsers().contains(u)) {
                        EmailDetails emailDetails =
                                new EmailDetails(u.getContactEmail(), dispatch.getName(), "", null);
                        BrowserMessage returnMsg = emailService.sendUserRequest(emailDetails, request);
                        message.getUsers().add(u);
                    }
                }
                messageService.save(message);
                break;
            default:
                for (User u : notify.getUsers()) {
                    if (!message.getUsers().contains(u)) {
                        EmailDetails emailDetails =
                                new EmailDetails(u.getContactEmail(), dispatch.getName(), "", null);
                        BrowserMessage returnMsg = emailService.sendSystemNotification(emailDetails, dispatch);
                        message.getUsers().add(u);
                    }
                }
        }
    }

}
