package net.dahliasolutions.controllers.app;

import lombok.RequiredArgsConstructor;
import net.dahliasolutions.models.*;
import net.dahliasolutions.models.mail.MailerCustomMessage;
import net.dahliasolutions.services.mail.MailerCustomMessageService;
import net.dahliasolutions.services.mail.NotificationMessageService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigInteger;
import java.util.Optional;

@Controller
@RequiredArgsConstructor
@RequestMapping("/app/mailer")
public class MobileAppEmailController {

    private final MailerCustomMessageService mailerCustomMessageService;
    private final NotificationMessageService notificationMessageService;
    private final AppServer appServer;

    @GetMapping("/content/{id}")
    public String getMailerContent(@PathVariable BigInteger id, Model model) {
        Optional<NotificationMessage> message = notificationMessageService.findById(id);
        if (message.isPresent()) {
            switch (message.get().getModule()) {
                case Messaging:
                    EventType newEvent = EventType.Custom;
                    System.out.println(newEvent);
                    message.get().setType(newEvent);
                    notificationMessageService.save(message.get());
                    // get message
                    Optional<MailerCustomMessage> customMessage = mailerCustomMessageService.findById(message.get().getNoteId());
                    model.addAttribute("baseURL", appServer.getBaseURL());
                    model.addAttribute("messageId", id);
                    if (customMessage.isPresent()) {
                        model.addAttribute("notification", customMessage.get());
                    } else {
                        model.addAttribute("notification", new MailerCustomMessage());
                    }
                    return "mailer/mailCustomNotificationViewer";
            }
        }
        return null;
    }
}
