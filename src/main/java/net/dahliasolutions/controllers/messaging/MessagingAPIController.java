package net.dahliasolutions.controllers.messaging;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import net.dahliasolutions.models.NotificationMessage;
import net.dahliasolutions.models.NotificationMessageModel;
import net.dahliasolutions.models.mail.MessageGroupEnum;
import net.dahliasolutions.models.user.User;
import net.dahliasolutions.services.mail.NotificationMessageService;
import net.dahliasolutions.services.user.UserService;
import org.springframework.web.bind.annotation.*;

import java.math.BigInteger;
import java.util.*;

@RestController
@RequestMapping("/api/v1/messaging")
@RequiredArgsConstructor
public class MessagingAPIController {

    private final NotificationMessageService notificationMessageService;
    private final UserService userService;

    @GetMapping("/message/{id}")
    public NotificationMessageModel getMessageById(@PathVariable BigInteger id) {
        Optional<NotificationMessage> notification = notificationMessageService.findById(id);
        NotificationMessageModel message = new NotificationMessageModel();
        // set from name
        String fromName = "System";
        Optional<User> fromUser = userService.findById(notification.get().getFromUserId());
        if (fromUser.isPresent()) {
            fromName = fromUser.get().getFullName();
        }
        if (notification.isPresent()) {
            message.setId(notification.get().getId());
            message.setSubject(notification.get().getSubject());
            message.setDateSent(notification.get().getDateSent());
            message.setRead(notification.get().isRead());
            message.setModule(notification.get().getModule().toString());
            message.setFromUser(fromName);
        }
        return message;
    }

    @GetMapping("/readstate/read/{id}")
    public int setMessageReadById(@PathVariable BigInteger id) {
        Optional<NotificationMessage> notification = notificationMessageService.findById(id);
        if (notification.isPresent()) {
            notification.get().setRead(true);
            notificationMessageService.save(notification.get());
            return 1;
        }
        return 0;
    }

    @GetMapping("/readstate/unread/{id}")
    public int setMessageUnreadById(@PathVariable BigInteger id) {
        Optional<NotificationMessage> notification = notificationMessageService.findById(id);
        if (notification.isPresent()) {
            notification.get().setRead(false);
            notificationMessageService.save(notification.get());
            return 1;
        }
        return 0;
    }

    @GetMapping("/messagegroups")
    public List<MessageGroupEnum> getMessageGroups() {
        return Arrays.asList(MessageGroupEnum.values());
    }
}
