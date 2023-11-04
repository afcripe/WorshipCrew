package net.dahliasolutions.controllers.messaging;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import net.dahliasolutions.models.BrowserMessage;
import net.dahliasolutions.models.NotificationMessage;
import net.dahliasolutions.models.NotificationMessageModel;
import net.dahliasolutions.models.UniversalSearchModel;
import net.dahliasolutions.models.campus.Campus;
import net.dahliasolutions.models.department.DepartmentCampus;
import net.dahliasolutions.models.department.DepartmentRegional;
import net.dahliasolutions.models.mail.EmailDetails;
import net.dahliasolutions.models.position.ChangeTemplateModel;
import net.dahliasolutions.models.position.PermissionTemplate;
import net.dahliasolutions.models.records.BigIntegerStringModel;
import net.dahliasolutions.models.records.CampusDepartmentModel;
import net.dahliasolutions.models.records.SingleStringModel;
import net.dahliasolutions.models.user.User;
import net.dahliasolutions.models.user.UserRoles;
import net.dahliasolutions.services.AuthService;
import net.dahliasolutions.services.campus.CampusService;
import net.dahliasolutions.services.department.DepartmentCampusService;
import net.dahliasolutions.services.department.DepartmentRegionalService;
import net.dahliasolutions.services.mail.EmailService;
import net.dahliasolutions.services.mail.NotificationMessageService;
import net.dahliasolutions.services.position.PermissionTemplateService;
import net.dahliasolutions.services.user.UserRolesService;
import net.dahliasolutions.services.user.UserService;
import org.springframework.security.core.context.SecurityContextHolder;
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
        if (notification.isPresent()) {
            message.setId(notification.get().getId());
            message.setSubject(notification.get().getSubject());
            message.setDateSent(notification.get().getDateSent());
            message.setRead(notification.get().isRead());
            message.setModule(notification.get().getModule().toString());
            message.setFromUser(notification.get().getUser().getFullName());
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
}
