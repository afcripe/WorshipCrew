package net.dahliasolutions.controllers.app;

import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import net.dahliasolutions.models.*;
import net.dahliasolutions.models.campus.Campus;
import net.dahliasolutions.models.department.DepartmentRegional;
import net.dahliasolutions.models.mail.MailerCustomMessage;
import net.dahliasolutions.models.order.OrderItem;
import net.dahliasolutions.models.order.OrderNote;
import net.dahliasolutions.models.order.OrderRequest;
import net.dahliasolutions.models.records.*;
import net.dahliasolutions.models.support.*;
import net.dahliasolutions.models.user.User;
import net.dahliasolutions.models.user.UserRoles;
import net.dahliasolutions.services.JwtService;
import net.dahliasolutions.services.campus.CampusService;
import net.dahliasolutions.services.department.DepartmentCampusService;
import net.dahliasolutions.services.department.DepartmentRegionalService;
import net.dahliasolutions.services.mail.MailerCustomMessageService;
import net.dahliasolutions.services.mail.NotificationMessageService;
import net.dahliasolutions.services.support.*;
import net.dahliasolutions.services.user.UserRolesService;
import net.dahliasolutions.services.user.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/app/messages")
public class MobileAppAPIMessagesController {

    private final JwtService jwtService;
    private final UserService userService;
    private final MailerCustomMessageService mailerCustomMessageService;
    private final NotificationMessageService notificationMessageService;
    private final UserRolesService rolesService;
    private final CampusService campusService;
    private final DepartmentRegionalService departmentRegionalService;
    private final DepartmentCampusService departmentCampusService;
    private final AppServer appServer;

    @GetMapping("/listnew")
    public ResponseEntity<List<NotificationMessageModel>> getTicketsByUser(
            @RequestParam Optional<String> system, @RequestParam Optional<String> read,
            @RequestParam Optional<String> draft, HttpServletRequest request) {

        APIUser apiUser = getUserFromToken(request);
        if (!apiUser.isValid()) {
            return new ResponseEntity<>(new ArrayList<>(), HttpStatus.FORBIDDEN);
        }

        // set default messages to display
        boolean showSystemMsg = false;
        boolean showAllMsg = false;
        boolean showDraft = false;

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
        if (draft.isPresent()) {
            if (!draft.get().toLowerCase().equals("false")) {
                showDraft = true;
            }
        }

        // get user messages
        List<NotificationMessage> notificationMessageList = notificationMessageService.getUserAll(apiUser.getUser());

        // convert to message model
        List<NotificationMessageModel> modelList = new ArrayList<>();
        if (!showDraft) {
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
        } else {
            List<MailerCustomMessage> customMessages = mailerCustomMessageService.findByUserAndDraft(apiUser.getUser(), true);
            for (MailerCustomMessage msg : customMessages) {
                NotificationMessageModel messageModel = new NotificationMessageModel(
                        msg.getId(),
                        msg.getSubject(),
                        null,
                        false,
                        "(Draft)",
                        EventModule.Messaging.toString(),
                        ""
                );
                modelList.add(messageModel);
            }
        }

        // sort by date and revers to put new on top
        modelList.sort(new Comparator<NotificationMessageModel>() {
            @Override
            public int compare(NotificationMessageModel msg1, NotificationMessageModel msg2) {
                int i;
                try {
                    i = msg1.getDateSent().compareTo(msg2.getDateSent());
                } catch (Exception e) {
                    i = 1;
                }
                return i;
            }
        });
        Collections.reverse(modelList);

        return new ResponseEntity<>(modelList, HttpStatus.OK);
    }

    @GetMapping("/message/{id}")
    public  ResponseEntity<NotificationMessageModel> getMessageBodyContent(@PathVariable BigInteger id, Model model, HttpServletRequest request) {

        APIUser apiUser = getUserFromToken(request);
        if (!apiUser.isValid()) {
            return new ResponseEntity<>(new NotificationMessageModel(), HttpStatus.FORBIDDEN);
        }

        // get message
        Optional<NotificationMessage> message = notificationMessageService.findById(id);
        NotificationMessageModel messageModel = new NotificationMessageModel();

        if (message.isPresent()) {
            Optional<User> fromUser = userService.findById(message.get().getFromUserId());
            String sendingUser = "System Message";
            if (fromUser.isPresent()) {
                sendingUser = fromUser.get().getFullName();
            }

            messageModel = new NotificationMessageModel(
                    message.get().getId(),
                    message.get().getSubject(),
                    message.get().getDateSent(),
                    message.get().isRead(),
                    sendingUser,
                    message.get().getModule().toString(),
                    ""
            );
        }
        return new ResponseEntity<>(messageModel, HttpStatus.OK);

    }


    private APIUser getUserFromToken(HttpServletRequest request) {
        final String authHeader = request.getHeader("Authorization");
        final String token;
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);
            try {
                Optional<User> currentUser = userService.findByUsername(jwtService.extractUsername(token));
                if (currentUser.isPresent()) {
                    if (jwtService.isTokenValid(token, currentUser.get())) {
                        return new APIUser(true, currentUser.get());
                    }
                }
            } catch (ExpiredJwtException e) {
                System.out.println("Token Expired");
            }
        }
        return new APIUser(false, new User());
    }

}
