package net.dahliasolutions.controllers.messaging;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import net.dahliasolutions.models.AppServer;
import net.dahliasolutions.models.NotificationMessage;
import net.dahliasolutions.models.NotificationMessageModel;
import net.dahliasolutions.models.position.PermissionTemplate;
import net.dahliasolutions.models.user.User;
import net.dahliasolutions.services.RedirectService;
import net.dahliasolutions.services.mail.NotificationMessageService;
import net.dahliasolutions.services.user.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigInteger;
import java.util.*;

@Controller
@RequestMapping("/messaging")
@RequiredArgsConstructor
public class MessagingController {

    private final NotificationMessageService notificationMessageService;
    private final UserService userService;
    private final RedirectService redirectService;
    private final AppServer appServer;

    @ModelAttribute
    public void addAttributes(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) auth.getPrincipal();
        model.addAttribute("moduleTitle", "Messaging");
        model.addAttribute("moduleLink", "/messaging");
        model.addAttribute("userId", user.getId());
        model.addAttribute("baseURL",appServer.getBaseURL());
    }

    @GetMapping("")
    public String getMessagingHome(@RequestParam Optional<String> system, @RequestParam Optional<String> read, Model model, HttpSession session) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) auth.getPrincipal();

        // set default messages to display
        boolean showSystemMsg = false;
        boolean showAllMsg = false;
        if (system.isPresent()) {
            if (!system.get().toLowerCase().equals("false")) {
                showSystemMsg = true;
            }
        }
        if (read.isPresent()) {
            if (!read.get().toLowerCase().equals("false")) {
                showAllMsg = true;
            }
        }

        // get user messages
        List<NotificationMessage> notificationMessageList = notificationMessageService.getUserAll(user);

        // convert to message model
        List<NotificationMessageModel> modelList = new ArrayList<>();
        for (NotificationMessage message : notificationMessageList) {
            // set from name
            Optional<User> fromUser = userService.findById(message.getFromUserId());
            String sendingUser = "System Message";
            if (fromUser.isPresent()) {
                sendingUser = fromUser.get().getFullName();
            }

            if (message.getFromUserId().equals(BigInteger.valueOf(0))) {
                if (showSystemMsg) {
                    if (showAllMsg) {
                        NotificationMessageModel messageModel = new NotificationMessageModel(
                                message.getId(),
                                message.getSubject(),
                                message.getDateSent(),
                                message.isRead(),
                                sendingUser,
                                message.getModule().toString(),
                                ""
                        );
                        modelList.add(messageModel);
                    } else if (!message.isRead()) {
                        NotificationMessageModel messageModel = new NotificationMessageModel(
                                message.getId(),
                                message.getSubject(),
                                message.getDateSent(),
                                message.isRead(),
                                sendingUser,
                                message.getModule().toString(),
                                ""
                        );
                        modelList.add(messageModel);
                    }
                }
            } else {
                if (showAllMsg) {
                    NotificationMessageModel messageModel = new NotificationMessageModel(
                            message.getId(),
                            message.getSubject(),
                            message.getDateSent(),
                            message.isRead(),
                            sendingUser,
                            message.getModule().toString(),
                            ""
                    );
                    modelList.add(messageModel);
                } else if (!message.isRead()) {
                    NotificationMessageModel messageModel = new NotificationMessageModel(
                            message.getId(),
                            message.getSubject(),
                            message.getDateSent(),
                            message.isRead(),
                            sendingUser,
                            message.getModule().toString(),
                            ""
                    );
                    modelList.add(messageModel);
                }
            }

        }

        // sort by date and revers to put new on top
        Collections.sort(modelList, new Comparator<NotificationMessageModel>() {
            @Override
            public int compare(NotificationMessageModel msg1, NotificationMessageModel msg2) {
                return msg1.getDateSent().compareTo(msg2.getDateSent());
            }
        });
        Collections.reverse(modelList);

        model.addAttribute("messageList", modelList);

        redirectService.setHistory(session, "/messaging");
        return "messaging/index";
    }

}
