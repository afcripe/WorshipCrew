package net.dahliasolutions.controllers.mail;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import net.dahliasolutions.models.*;
import net.dahliasolutions.models.mail.EmailDetails;
import net.dahliasolutions.models.mail.MailerCustomMessage;
import net.dahliasolutions.models.mail.MailerLinks;
import net.dahliasolutions.models.order.OrderItem;
import net.dahliasolutions.models.order.OrderNote;
import net.dahliasolutions.models.order.OrderRequest;
import net.dahliasolutions.models.order.OrderStatus;
import net.dahliasolutions.models.records.SingleStringModel;
import net.dahliasolutions.models.support.Ticket;
import net.dahliasolutions.models.support.TicketNote;
import net.dahliasolutions.models.user.ChangePasswordModel;
import net.dahliasolutions.models.user.User;
import net.dahliasolutions.services.EventService;
import net.dahliasolutions.services.mail.EmailService;
import net.dahliasolutions.services.mail.MailerCustomMessageService;
import net.dahliasolutions.services.mail.MailerLinksService;
import net.dahliasolutions.services.mail.NotificationMessageService;
import net.dahliasolutions.services.order.OrderItemService;
import net.dahliasolutions.services.order.OrderNoteService;
import net.dahliasolutions.services.order.OrderService;
import net.dahliasolutions.services.support.TicketNoteService;
import net.dahliasolutions.services.support.TicketService;
import net.dahliasolutions.services.user.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Optional;

@Controller
@RequiredArgsConstructor
@RequestMapping("/mailer")
public class EmailController {

    private final MailerLinksService mailerLinksService;
    private final UserService userService;
    private final OrderService orderService;
    private final OrderItemService orderItemService;
    private final OrderNoteService orderNoteService;
    private final TicketService ticketService;
    private final TicketNoteService ticketNoteService;
    private final EmailService emailService;
    private final NotificationMessageService messageService;
    private final EventService eventService;
    private final MailerCustomMessageService mailerCustomMessageService;
    private final NotificationMessageService notificationMessageService;
    private final AppServer appServer;

    @GetMapping("")
    public String testMailer(){
        return "documentation";
    }
    @GetMapping("/")
    public String testMailerTwo(){
        return "documentation";
    }

    @GetMapping("/{randomString}")
    public String getSetPasswordForm(@PathVariable String randomString, Model model) {
        Optional<MailerLinks> mailerLinks = mailerLinksService.findByRandomLinkString(randomString);
        if (mailerLinks.isEmpty()) {
            model.addAttribute("errorMessage", "Link not Valid!");
            return "error";
        }
        if (mailerLinks.get().getExpiration().isBefore(LocalDateTime.now()) || mailerLinks.get().isForceExpire()) {
            model.addAttribute("errorMessage", " The link has Expired!");
            return "error";
        }

        User user = userService.findById((mailerLinks.get().getUserId())).orElse(null);
        model.addAttribute("userDetails", user);
        model.addAttribute("randomString", randomString);

        switch (mailerLinks.get().getServiceMethod()) {
            case "setPassword":
            case "resetPassword":
                return "mailer/setPassword";
            case "acknowledgeRequest":
                updateOrderRequest(mailerLinks.get());
                return "mailer/acknowledge";
            case "acknowledgeItem":
                updateOrderRequestItem(mailerLinks.get());
                return "mailer/acknowledge";
            case "acknowledgeTicket":
                updateTicketNote(mailerLinks.get());
                return "mailer/acknowledge";
            case "agentAcceptTicket":
                Ticket ticket = updateTicketAgent(mailerLinks.get());
                model.addAttribute("ticket", ticket);
                return "mailer/acceptTicket";
            default:
                model.addAttribute("errorMessage", "Link not Valid!");
                return "error";
        }
    }

    @PostMapping("/setpassword")
    public String setNewPassword(@ModelAttribute ChangePasswordModel changePasswordModel, HttpSession session, Model model) {
        MailerLinks mailerLinks = mailerLinksService.findByRandomLinkString(changePasswordModel.currentPassword()).orElse(null);
        if (mailerLinks == null) {
            model.addAttribute("errorMessage", "Link not Valid!");
            return "error";
        }
        if (mailerLinks.getExpiration().isBefore(LocalDateTime.now()) || mailerLinks.isForceExpire()) {
            model.addAttribute("errorMessage", "Link has Expired!");
            return "error";
        }
        if (mailerLinks.getServiceMethod().equals("setPassword") || mailerLinks.getServiceMethod().equals("resetPassword")) {
            User user = userService.findById((mailerLinks.getUserId())).orElse(null);
            if(user == null){
                model.addAttribute("errorMessage", "User Not Found");
                return "error";
            }

            if (!changePasswordModel.newPassword().equals(changePasswordModel.confirmPassword())) {
                session.setAttribute("msgError", "Passwords Do Not Match!");
                return "redirect:/mailer/"+changePasswordModel.currentPassword();
            } else {
                userService.updateActivateUserPasswordById(user.getId(), changePasswordModel.newPassword());
                mailerLinks.setForceExpire(true);
                mailerLinksService.save(mailerLinks);
            }
            return "redirect:/login";
        }

        model.addAttribute("errorMessage", "Link not valid for setting password!");
        return "error";
    }


    private void updateOrderRequest(MailerLinks mailerLinks) {
        Optional<OrderRequest> orderRequest = orderService.findById(mailerLinks.getServiceId());
        Optional<User> supervisor = userService.findById(mailerLinks.getUserId());

        if (orderRequest.isPresent()) {
            User user = orderRequest.get().getUser();
            String statusNote = "Request Marked Received by "+supervisor.get().getFirstName()+" "+supervisor.get().getLastName();
            orderRequest.get().setOrderStatus(OrderStatus.Received);
            orderService.save(orderRequest.get());
            OrderNote orderNote = orderNoteService.createOrderNote(new OrderNote(
                    null,
                    orderRequest.get().getId(),
                    null,
                    statusNote,
                    BigInteger.valueOf(0),
                    orderRequest.get().getOrderStatus(),
                    supervisor.get()));

//            EmailDetails emailDetailsUser =
//                    new EmailDetails(BigInteger.valueOf(0), user.getContactEmail(),"Your Request Has Been Received", "", null );
//            BrowserMessage returnMsg = emailService.sendUserRequest(emailDetailsUser, orderRequest.get());

            // notify user of request status change
            NotificationMessage returnMsgUser = messageService.createMessage(
                    new NotificationMessage(
                            null,
                            "["+orderRequest.get().getId().toString()+"] Request Marked Received",
                            orderRequest.get().getId().toString(),
                            BigInteger.valueOf(0),
                            null,
                            false,
                            false,
                            null,
                            false,
                            BigInteger.valueOf(0),
                            EventModule.Request,
                            EventType.Updated,
                            user,
                            orderNote.getId()
                    ));
        }

        mailerLinks.setForceExpire(true);
        mailerLinksService.save(mailerLinks);
    }

    private void updateOrderRequestItem(MailerLinks mailerLinks) {
        Optional<OrderItem> requestItem = orderItemService.findById(mailerLinks.getServiceId());
        Optional<User> supervisor = userService.findById(mailerLinks.getUserId());

        if (requestItem.isPresent()) {
            User OrderSupervisor = requestItem.get().getOrderRequest().getSupervisor();
            String statusNote = "Request Item Marked Received by "+supervisor.get().getFirstName()+" "+supervisor.get().getLastName();
            // update item status
            requestItem.get().setItemStatus(OrderStatus.Received);
            orderItemService.save(requestItem.get());
            // add new note to order
            OrderNote orderNote = orderNoteService.createOrderNote(new OrderNote(
                    null,
                    requestItem.get().getOrderRequest().getId(),
                    null,
                    statusNote,
                    requestItem.get().getId(),
                    requestItem.get().getItemStatus(),
                    supervisor.get()));

//            EmailDetails emailDetailsSupervisor =
//                    new EmailDetails(BigInteger.valueOf(0), OrderSupervisor.getContactEmail(),"Request Item was Received", "", null );
//            BrowserMessage returnMsg2 = emailService.sendUserRequest(emailDetailsSupervisor, requestItem.get().getOrderRequest());

            // notify user of request status change
            NotificationMessage returnMsgUser = messageService.createMessage(
                    new NotificationMessage(
                            null,
                            "["+requestItem.get().getOrderRequest().getId().toString()+"] Request Marked Received",
                            requestItem.get().getOrderRequest().getId().toString(),
                            requestItem.get().getId(),
                            null,
                            false,
                            false,
                            null,
                            false,
                            BigInteger.valueOf(0),
                            EventModule.Request,
                            EventType.Updated,
                            OrderSupervisor,
                            orderNote.getId()
                    ));
        }

        mailerLinks.setForceExpire(true);
        mailerLinksService.save(mailerLinks);
    }

    private void updateTicketNote(MailerLinks mailerLinks) {
        Optional<Ticket> ticket = ticketService.findById(mailerLinks.getTicketId());
        Optional<User> agent = userService.findById(mailerLinks.getUserId());

        if (ticket.isPresent() && agent.isPresent()) {
            String detail = agent.get().getFullName()+" has acknowledged the ticket.";
            TicketNote note = ticketNoteService.createTicketNote(
                    new TicketNote(null,
                            null,
                            true,
                            true,
                            detail,
                            new ArrayList<>(),
                            agent.get(),
                            ticket.get()));
            ticket.get().getNotes().add(note);
            ticketService.save(ticket.get());

            // send any additional notifications
            AppEvent notifyEvent = eventService.createEvent(new AppEvent(
                    null,
                    "Ticket "+ticket.get().getId()+" was acknowledged by "+agent.get().getFullName(),
                    "Ticket "+ticket.get().getId()+" was acknowledged by "+agent.get().getFullName()+" via email link.",
                    ticket.get().getId(),
                    EventModule.Support,
                    EventType.New,
                    new ArrayList<>()
            ));
        }

        mailerLinks.setForceExpire(true);
        mailerLinksService.save(mailerLinks);
    }

    private Ticket updateTicketAgent(MailerLinks mailerLinks) {
        Optional<Ticket> ticket = ticketService.findById(mailerLinks.getTicketId());
        Optional<User> agent = userService.findById(mailerLinks.getUserId());

        if (ticket.isPresent() && agent.isPresent()) {
            if (ticket.get().getAgent() == null) {
                // set the agent
                ticket.get().setAgent(agent.get());
                // add a note
                String detail = agent.get().getFullName()+" accepted the ticket and has been assigned as primary agent.";
                TicketNote note = ticketNoteService.createTicketNote(
                        new TicketNote(null,
                                null,
                                true,
                                true,
                                detail,
                                new ArrayList<>(),
                                agent.get(),
                                ticket.get()));
                ticket.get().getNotes().add(note);
                ticketService.save(ticket.get());

                // send any additional notifications
                AppEvent notifyEvent = eventService.createEvent(new AppEvent(
                        null,
                        "Ticket "+ticket.get().getId()+" was accepted by "+agent.get().getFullName(),
                        "Ticket "+ticket.get().getId()+" was accepted by "+agent.get().getFullName(),
                        ticket.get().getId(),
                        EventModule.Support,
                        EventType.New,
                        new ArrayList<>()
                ));
            }

            // force expire mailerLink
            mailerLinks.setForceExpire(true);

            mailerLinksService.save(mailerLinks);

            return ticket.get();
        }
        return new Ticket();
    }
}
