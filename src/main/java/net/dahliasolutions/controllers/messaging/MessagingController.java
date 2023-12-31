package net.dahliasolutions.controllers.messaging;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import net.dahliasolutions.models.*;
import net.dahliasolutions.models.mail.MailerCustomMessage;
import net.dahliasolutions.models.mail.MailerCustomMessageModel;
import net.dahliasolutions.models.order.OrderItem;
import net.dahliasolutions.models.order.OrderNote;
import net.dahliasolutions.models.order.OrderRequest;
import net.dahliasolutions.models.support.*;
import net.dahliasolutions.models.user.User;
import net.dahliasolutions.models.user.UserRoles;
import net.dahliasolutions.services.RedirectService;
import net.dahliasolutions.services.mail.AppEventService;
import net.dahliasolutions.services.mail.MailerCustomMessageService;
import net.dahliasolutions.services.mail.NotificationMessageService;
import net.dahliasolutions.services.order.OrderItemService;
import net.dahliasolutions.services.order.OrderNoteService;
import net.dahliasolutions.services.order.OrderService;
import net.dahliasolutions.services.support.TicketNoteService;
import net.dahliasolutions.services.support.TicketService;
import net.dahliasolutions.services.user.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigInteger;
import java.util.*;

@Controller
@RequestMapping("/messaging")
@RequiredArgsConstructor
public class MessagingController {

    private final MailerCustomMessageService mailerCustomMessageService;
    private final NotificationMessageService notificationMessageService;
    private final UserService userService;
    private final RedirectService redirectService;
    private final OrderService orderService;
    private final OrderItemService orderItemService;
    private final OrderNoteService orderNoteService;
    private final TicketService ticketService;
    private final TicketNoteService ticketNoteService;
    private final AppEventService appEventService;
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
    public String getMessagingHome(
            @RequestParam Optional<String> system, @RequestParam Optional<String> read, @RequestParam Optional<String> draft,
            Model model, HttpSession session) {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

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
        List<NotificationMessage> notificationMessageList = notificationMessageService.getUserAll(user);

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
            List<MailerCustomMessage> customMessages = mailerCustomMessageService.findByUserAndDraft(user, true);
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

        model.addAttribute("messageList", modelList);

        redirectService.setHistory(session, "/messaging");
        return "messaging/index";
    }


    @GetMapping("/new")
    public String getMessagingCreate(Model model, HttpSession session) {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        model.addAttribute("groupList", permissionList(user));
        model.addAttribute("message", new MailerCustomMessageModel(
                BigInteger.valueOf(0), "", true, "", user.getId(), "", ""));

        redirectService.setHistory(session, "/messaging");
        return "messaging/newMessage";
    }


    @GetMapping("/new/{id}")
    public String getMessagingEdit(@PathVariable BigInteger id, Model model, HttpSession session) {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        MailerCustomMessageModel messageModel;
        Optional<MailerCustomMessage> message = mailerCustomMessageService.findById(id);
        if (message.isPresent()) {
            messageModel = mailerCustomMessageService.convertEntityToModel(message.get());
        } else {
            messageModel = new MailerCustomMessageModel(BigInteger.valueOf(0), "", true, "", user.getId(), "", "");
        }

        model.addAttribute("groupList", permissionList(user));
        model.addAttribute("message", messageModel);

        redirectService.setHistory(session, "/messaging");
        return "messaging/newMessage";
    }

    @GetMapping("/content/{id}")
    public String getMessageBodyContent(@PathVariable BigInteger id, Model model) {
        // get message
        Optional<NotificationMessage> message = notificationMessageService.findById(id);
        if (message.isPresent()) {
            switch (message.get().getModule()) {
                case Request:
                    BigInteger orderId = BigInteger.valueOf(0);
                    if(!message.get().getModuleId().isBlank()) {
                        orderId = new BigInteger(message.get().getModuleId());
                    }
                    OrderRequest newRequest = orderService.findById(orderId).orElse(new OrderRequest());
                    OrderItem requestItem = new OrderItem();
                    if (!message.get().getItemId().equals(BigInteger.valueOf(0))) {
                        requestItem = orderItemService.findById(message.get().getItemId()).get();
                    }

                    if (message.get().getEventId() != null) {
                        AppEvent appEvent = appEventService.findAppEventById(message.get().getEventId()).get();
                        // return appEvent body
                        model.addAttribute("baseURL", appServer.getBaseURL());
                        model.addAttribute("messageId", id);
                        model.addAttribute("notification", appEvent);
                        model.addAttribute("emailSubject", message.get().getSubject());
                        return "mailer/mailNotification";
                    } else {
                        switch (message.get().getType()) {
                            case New -> {
                                model.addAttribute("baseURL", appServer.getBaseURL());
                                model.addAttribute("messageId", id);
                                model.addAttribute("orderRequest", requestItem);
                                model.addAttribute("emailSubject", message.get().getSubject());
                                model.addAttribute("webLink", "/request/order/"+newRequest.getId());
                                return "mailer/mailSupervisorRequestViewer";
                            }
                            case NewItem -> {
                                model.addAttribute("baseURL", appServer.getBaseURL());
                                model.addAttribute("messageId", id);
                                model.addAttribute("requestItem", requestItem);
                                model.addAttribute("emailSubject", message.get().getSubject());
                                model.addAttribute("webLink", "/request/order/"+newRequest.getId());
                                return "mailer/mailItemRequestViewer";
                            }
                            case ItemUpdated -> {
                                OrderNote note = orderNoteService.findById(message.get().getNoteId()).get();
                                model.addAttribute("baseURL", appServer.getBaseURL());
                                model.addAttribute("messageId", id);
                                model.addAttribute("requestItem", requestItem);
                                model.addAttribute("orderNote", note);
                                model.addAttribute("emailSubject", message.get().getSubject());
                                return "mailer/mailItemUpdate";
                            }
                            case Updated -> {
                                model.addAttribute("baseURL", appServer.getBaseURL());
                                model.addAttribute("messageId", id);
                                model.addAttribute("orderRequest", requestItem);
                                model.addAttribute("emailSubject", message.get().getSubject());
                                return "mailer/mailUserRequest";
                            }
                        }
                    }
                    break;
                case Support:
                    Ticket ticket = ticketService.findById(message.get().getModuleId()).orElse(new Ticket());
                    TicketNote note = new TicketNote();
                    if (!message.get().getNoteId().equals(BigInteger.valueOf(0))) {
                        note = ticketNoteService.findById(message.get().getNoteId()).get();
                    }

                    switch (message.get().getType()) {
                        case New -> {
                            model.addAttribute("baseURL", appServer.getBaseURL());
                            model.addAttribute("messageId", id);
                            model.addAttribute("ticket", ticket);
                            model.addAttribute("ticketNote", note);
                            model.addAttribute("emailSubject", message.get().getSubject());
                            model.addAttribute("webLink", "/support/ticket/"+ticket.getId());
                            return "mailer/mailAgentTicketViewer";
                        }
                        case NewList -> {
                            model.addAttribute("baseURL", appServer.getBaseURL());
                            model.addAttribute("messageId", id);
                            model.addAttribute("ticket", ticket);
                            model.addAttribute("ticketNote", note);
                            model.addAttribute("agentList", ticket.getAgentList());
                            model.addAttribute("emailSubject", message.get().getSubject());
                            model.addAttribute("webLink", "/support/ticket/"+ticket.getId());
                            return "mailer/mailAgentListTicketViewer";
                        }
                        case Updated -> {
                            model.addAttribute("baseURL", appServer.getBaseURL());
                            model.addAttribute("messageId", id);
                            model.addAttribute("ticket", ticket);
                            model.addAttribute("ticketNote", note);
                            model.addAttribute("emailSubject", message.get().getSubject());
                            return "mailer/mailAgentUpdateTicket";
                        }
                    }
                    break;
                case Store:
                case User:
                    if (message.get().getEventId() != null) {
                        AppEvent appEvent = appEventService.findAppEventById(message.get().getEventId()).get();
                        model.addAttribute("baseURL", appServer.getBaseURL());
                        model.addAttribute("messageId", id);
                        model.addAttribute("notification", appEvent);
                        model.addAttribute("emailSubject", message.get().getSubject());
                        return "mailer/mailNotification";
                    }
                    break;
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

        // determine template

        // return template
        return "mailer/mailNotification";

    }

    @GetMapping("/settings")
    public String goSupportSettings(Model model, HttpSession session) {

        redirectService.setHistory(session, "/messaging/settings");
        return "messaging/settings";
    }

    // get edit permission
    private List<String> permissionList(User currentUser) {
        Collection<UserRoles> roles = currentUser.getUserRoles();

        List<String> permList = new ArrayList<>();
        permList.add("All Users");

        for (UserRoles role : roles) {
            if (role.getName().equals("CAMPUS_WRITE") || role.getName().equals("CAMPUS_READ")
                    || role.getName().equals("ADMIN_WRITE") || role.getName().equals("USER_SUPERVISOR")) {
                if (!permList.contains("Campus Users")) { permList.add("Campus Users"); }
                if (!permList.contains("Campus Department Directors")) { permList.add("Campus Department Directors"); }
            }
            if (role.getName().equals("DIRECTOR_WRITE") || role.getName().equals("DIRECTOR_READ")
                    || role.getName().equals("ADMIN_WRITE") || role.getName().equals("USER_SUPERVISOR")) {
                if (!permList.contains("Department Users")) { permList.add("Department Users"); }
                if (!permList.contains("Campus Department Directors")) {permList.add("Campus Department Directors");}
            }
            if (role.getName().equals("ADMIN_WRITE") || role.getName().equals("USER_SUPERVISOR")) {
                if (!permList.contains("Campus Directors")) { permList.add("Campus Directors"); }
                if (!permList.contains("Campus Directors")) { permList.add("Campus Directors"); }
            }

            /*

            if (role.getName().equals("USER_WRITE") || role.getName().equals("USER_READ")
                    || role.getName().equals("ADMIN_WRITE") || role.getName().equals("USER_SUPERVISOR")) {
                permList.add("NA");
            }

                select Individuals

                campus department users

                campus
                    campus users
                    campus department users
                    campus department Directors

                department
                    department users
                    department campus users
                    department campus Directors

                campus leads
                department lead
                all users

             */

        }
        return permList;
    }
    private String permissionType(User currentUser) {
        Collection<UserRoles> roles = currentUser.getUserRoles();
        String typeString = "Campus Users";
        int priority = 0;
        for (UserRoles role : roles) {
            if (role.getName().equals("USER_WRITE") || role.getName().equals("USER_READ")) {
                if (priority < 1) {
                    typeString = "Campus Department Users";
                    priority = 1;
                }
            } else if (role.getName().equals("CAMPUS_WRITE") || role.getName().equals("CAMPUS_READ")) {
                if (priority < 2) {
                    typeString = "Campus Users";
                    priority = 2;
                }
            }   else if (role.getName().equals("DIRECTOR_WRITE") || role.getName().equals("DIRECTOR_READ")) {
                if (priority < 3) {
                    typeString = "Department Users";
                    priority = 3;
                }
            } else if (role.getName().equals("ADMIN_WRITE") || role.getName().equals("USER_SUPERVISOR")) {
                if (priority < 4) {
                    typeString = "All Users";
                    priority = 4;
                }
            }
        }
        return typeString;
    }

    private List<User> userList(User currentUser) {
        String permission = permissionType(currentUser);
        List<User> userList;
        switch (permission){
            case "All Users":
                userList = userService.findAll();
                break;
            case "Department Users":
                userList = userService.findAllByDepartmentAndDeleted(currentUser.getDepartment().getRegionalDepartment(), false);
                break;
            case "Campus Users":
                userList = userService.findAllByCampus(currentUser.getCampus());
                break;
            default:
                userList = userService.findAllByDepartmentCampus(currentUser.getDepartment());
        }
        return userList;
    }
}
