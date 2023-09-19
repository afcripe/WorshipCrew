package net.dahliasolutions.services.mail;

import lombok.RequiredArgsConstructor;
import net.dahliasolutions.data.NotificationMessageRepository;
import net.dahliasolutions.models.*;
import net.dahliasolutions.models.mail.EmailDetails;
import net.dahliasolutions.models.order.OrderItem;
import net.dahliasolutions.models.order.OrderNote;
import net.dahliasolutions.models.order.OrderRequest;
import net.dahliasolutions.models.support.Ticket;
import net.dahliasolutions.models.support.TicketNote;
import net.dahliasolutions.models.user.UserEndpoint;
import net.dahliasolutions.models.user.UserNotificationSubscribe;
import net.dahliasolutions.services.FirebaseMessagingService;
import net.dahliasolutions.services.order.OrderItemService;
import net.dahliasolutions.services.order.OrderNoteService;
import net.dahliasolutions.services.order.OrderService;
import net.dahliasolutions.services.support.TicketNoteService;
import net.dahliasolutions.services.support.TicketService;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class NotificationMessageService implements NotificationMessageServiceInterface{

    private final NotificationMessageRepository messageRepository;
//    private final PushService pushService;
    private final FirebaseMessagingService firebaseMessagingService;
    private final EmailService emailService;
    private final OrderService orderService;
    private final OrderItemService orderItemService;
    private final OrderNoteService orderNoteService;
    private final TicketService ticketService;
    private final TicketNoteService ticketNoteService;
    private final AppServer appServer;

    @Override
    public NotificationMessage createMessage(NotificationMessage message) {
        return messageRepository.save(message);
    }

    @Override
    public BrowserMessage sendMessage(NotificationMessage message) {
        BrowserMessage notificationStatus = new BrowserMessage();

        for (UserNotificationSubscribe subscription : message.getUser().getSubscriptions()) {
            switch (subscription.getEndPoint()) {
                case Push -> {
                    notificationStatus = sendPush(message);
                }
                case Email -> {
                    notificationStatus = sendEmail(message);
                }
            }
        }

        if (notificationStatus.getMsgType().equals("msgSuccess")) {
            System.out.println(notificationStatus);
            message.setDateSent(LocalDateTime.now());
            message.setSent(true);
            messageRepository.save(message);
        }
        return notificationStatus;
    }

    @Override
    public List<NotificationMessage> getUnsentMessages() {
        return messageRepository.findAllBySent(false);
    }

    @Override
    public void save(NotificationMessage message) {
        messageRepository.save(message);
    }

    @Override
    public void sendAllMessages() {
        List<NotificationMessage> unsentMessages = getUnsentMessages();
        for (NotificationMessage message : unsentMessages) {
            sendMessage(message);
        }
    }

    private BrowserMessage sendPush(NotificationMessage message) {
        BrowserMessage notificationStatus = new BrowserMessage("msgScheduled", "Message Blackout Period. Send Delayed.");

        Map<String, String> data = Map.ofEntries(
                Map.entry("link", appServer.getBaseURL()+"/app"),
                Map.entry("module", message.getModule().toString().toLowerCase()),
                Map.entry("moduleId", message.getModuleId())
        );

        // ToDo - Blackouts

        if (message.getModule().equals(EventModule.Request)) {
            switch (message.getType()) {
                case New -> {
                    for (UserEndpoint ep : message.getUser().getEndpoints()) {
                        FirebaseMessage fireMessage = new FirebaseMessage();
                                        fireMessage.setRecipientToken(ep.getToken());
                                        fireMessage.setTitle("New Request");
                                        fireMessage.setBody(message.getSubject());
                                        fireMessage.setData(data);
                        firebaseMessagingService.sendNotificationByToken(fireMessage);
                    }
                }
                case NewItem -> {
                    for (UserEndpoint ep : message.getUser().getEndpoints()) {
                        FirebaseMessage fireMessage = new FirebaseMessage();
                                        fireMessage.setRecipientToken(ep.getToken());
                                        fireMessage.setTitle("New Request Item");
                                        fireMessage.setBody(message.getSubject());
                                        fireMessage.setData(data);
                        firebaseMessagingService.sendNotificationByToken(fireMessage);
                    }
                }
                case ItemUpdated -> {
                    for (UserEndpoint ep : message.getUser().getEndpoints()) {
                        FirebaseMessage fireMessage = new FirebaseMessage();
                                        fireMessage.setRecipientToken(ep.getToken());
                                        fireMessage.setTitle("Request Item Updated");
                                        fireMessage.setBody(message.getSubject());
                                        fireMessage.setData(data);
                        firebaseMessagingService.sendNotificationByToken(fireMessage);
                    }
                }
                case Updated -> {
                    for (UserEndpoint ep : message.getUser().getEndpoints()) {
                        FirebaseMessage fireMessage = new FirebaseMessage();
                                        fireMessage.setRecipientToken(ep.getToken());
                                        fireMessage.setTitle("Request Updated");
                                        fireMessage.setBody(message.getSubject());
                                        fireMessage.setData(data);
                        firebaseMessagingService.sendNotificationByToken(fireMessage);
                    }
                }
                case Cancelled -> {
                    for (UserEndpoint ep : message.getUser().getEndpoints()) {
                        FirebaseMessage fireMessage = new FirebaseMessage();
                                        fireMessage.setRecipientToken(ep.getToken());
                                        fireMessage.setTitle("Request Cancelled");
                                        fireMessage.setBody(message.getSubject());
                                        fireMessage.setData(data);
                        firebaseMessagingService.sendNotificationByToken(fireMessage);
                    }
                }
            }
        }

        if (message.getModule().equals(EventModule.Support)) {
            switch (message.getType()) {
                case New, NewList -> {
                    for (UserEndpoint ep : message.getUser().getEndpoints()) {
                        FirebaseMessage fireMessage = new FirebaseMessage();
                        fireMessage.setRecipientToken(ep.getToken());
                        fireMessage.setTitle("New Ticket");
                        fireMessage.setBody(message.getSubject());
                        fireMessage.setData(data);
                        firebaseMessagingService.sendNotificationByToken(fireMessage);
                    }
                }
                case Updated -> {
                    for (UserEndpoint ep : message.getUser().getEndpoints()) {
                        FirebaseMessage fireMessage = new FirebaseMessage();
                        fireMessage.setRecipientToken(ep.getToken());
                        fireMessage.setTitle("Ticket Updated");
                        fireMessage.setBody(message.getSubject());
                        fireMessage.setData(data);
                        firebaseMessagingService.sendNotificationByToken(fireMessage);
                    }
                }
            }

        }

        return new BrowserMessage("msgSuccess", "Message Sent.");
    }

    private BrowserMessage sendEmail(NotificationMessage message) {
        BrowserMessage notificationStatus = new BrowserMessage("msgScheduled", "Message Blackout Period. Send Delayed.");

        // ToDo - Blackouts

        if (message.getModule().equals(EventModule.Request)) {
            BigInteger orderId = new BigInteger(message.getModuleId());
            OrderRequest newRequest = orderService.findById(orderId).get();
            OrderItem requestItem = new OrderItem();
            if (!message.getItemId().equals(BigInteger.valueOf(0))) {
                requestItem = orderItemService.findById(message.getItemId()).get();
            }

            switch (message.getType()) {
                case New -> {
                    EmailDetails emailDetailsSupervisor =
                            new EmailDetails(message.getUser().getContactEmail(), message.getSubject(), "", null);
                    emailService.sendSupervisorRequest(emailDetailsSupervisor, newRequest, message.getUser().getId());
                }
                case NewItem -> {
                    EmailDetails emailDetailsSupervisor =
                            new EmailDetails(message.getUser().getContactEmail(), message.getSubject(), "", null );
                    emailService.sendSupervisorItemRequest(emailDetailsSupervisor, requestItem, requestItem.getOrderRequest().getSupervisor().getId());
                }
                case ItemUpdated -> {
                    OrderNote note = orderNoteService.findById(message.getNoteId()).get();
                    EmailDetails emailDetailsSupervisor =
                            new EmailDetails(message.getUser().getContactEmail(), message.getSubject(), "", null );
                    emailService.sendItemUpdate(emailDetailsSupervisor, requestItem, note);
                }
                case Updated -> {
                    EmailDetails emailDetailsSupervisor =
                            new EmailDetails(message.getUser().getContactEmail(), message.getSubject(), "", null );
                    emailService.sendUserRequest(emailDetailsSupervisor, newRequest);
                }
            }
        }

        if (message.getModule().equals(EventModule.Support)) {
            Ticket ticket = ticketService.findById(message.getModuleId()).get();
            TicketNote note = new TicketNote();
            if (!message.getNoteId().equals(BigInteger.valueOf(0))) {
                note = ticketNoteService.findById(message.getNoteId()).get();
            }

            switch (message.getType()) {
                case New -> {
                    EmailDetails emailDetailsAgent =
                            new EmailDetails(message.getUser().getContactEmail(), message.getSubject(), "", null);
                    emailService.sendAgentTicket(emailDetailsAgent, ticket, note, message.getUser().getId());
                }
                case NewList -> {
                    EmailDetails emailDetailsAgent =
                            new EmailDetails(message.getUser().getContactEmail(), message.getSubject(), "", null);
                    emailService.sendAgentListTicket(emailDetailsAgent, ticket, note, message.getUser().getId());
                }
                case Updated -> {
                    EmailDetails emailDetailsAgent =
                            new EmailDetails(message.getUser().getContactEmail(), message.getSubject(), "", null );
                    emailService.sendAgentUpdateTicket(emailDetailsAgent, ticket, note);
                }
            }

        }

        return new BrowserMessage("msgSuccess", "Message Sent.");
    }
}


//    @Override
//    public BrowserMessage sendMessage(NotificationMessage message) {
//        // get user and determine endpoint
//        if (message.getUser().getNotificationEndPoint().equals(NotificationEndPoint.Push)) {
//            // send Push
//            // determine module for id parsing
//            if (message.getModule().equals(EventModule.Request)) {
//                BigInteger orderId = new BigInteger(message.getModuleId());
//                new PushMessage("request", message.getModuleId(), message.getSubject());
//                pushService.sendPushMessageToUser(
//                        new PushMessage("request", message.getModuleId(), message.getSubject()),
//                        message.getUser().getUsername());
//                FirebaseMessage fireMessage = new FirebaseMessage();
//                fireMessage.setRecipientToken(message.getUser().getSwToken());
//                fireMessage.setTitle("DWC Notification");
//                fireMessage.setBody(message.getSubject());
//                firebaseMessagingService.sendNotificationByToken(fireMessage);
//            }
//        } else {
//            // send Email
//            // determine module for id parsing
//            if (message.getModule().equals(EventModule.Request)) {
//                // determine if Request or Item
//                if (message.getType().equals(NotificationType.New)) {
//                    BigInteger orderId = new BigInteger(message.getModuleId());
//                    OrderRequest newRequest = orderService.findById(orderId).get();
//                    // send Email
//                    EmailDetails emailDetailsSupervisor =
//                            new EmailDetails(message.getUser().getContactEmail(), "A New Request", "", null);
//                    emailService.sendSupervisorRequest(emailDetailsSupervisor, newRequest, message.getUser().getId());
//                } else if (message.getType().equals(NotificationType.NewItem)) {
//
//                }
//            }
//        }
//
//        message.setDateSent(LocalDateTime.now());
//        message.setSent(true);
//        messageRepository.save(message);
//
//        return null;
//    }

// Websocket
//                new PushMessage("request", message.getModuleId(), message.getSubject());
//                pushService.sendPushMessageToUser(
//                        new PushMessage("request", message.getModuleId(), message.getSubject()),
//                        message.getUser().getUsername());
