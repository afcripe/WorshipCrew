package net.dahliasolutions.services.mail;

import lombok.RequiredArgsConstructor;
import net.dahliasolutions.data.NotificationMessageRepository;
import net.dahliasolutions.models.*;
import net.dahliasolutions.models.mail.EmailDetails;
import net.dahliasolutions.models.order.OrderRequest;
import net.dahliasolutions.models.user.UserEndpoint;
import net.dahliasolutions.services.FirebaseMessagingService;
import net.dahliasolutions.services.PushService;
import net.dahliasolutions.services.order.OrderItemService;
import net.dahliasolutions.services.order.OrderService;
import net.dahliasolutions.services.support.TicketService;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationMessageService implements NotificationMessageServiceInterface{

    private final NotificationMessageRepository messageRepository;
    private final PushService pushService;
    private final FirebaseMessagingService firebaseMessagingService;
    private final EmailService emailService;
    private final OrderService orderService;
    private final OrderItemService orderItemService;
    private final TicketService ticketService;

    @Override
    public NotificationMessage createMessage(NotificationMessage message) {
        return messageRepository.save(message);
    }

    @Override
    public BrowserMessage sendMessage(NotificationMessage message) {

        // send Push
        // determine module for id parsing
        if (message.getModule().equals(EventModule.Request)) {
            // Websocket
                BigInteger orderId = new BigInteger(message.getModuleId());
                new PushMessage("request", message.getModuleId(), message.getSubject());
                pushService.sendPushMessageToUser(
                        new PushMessage("request", message.getModuleId(), message.getSubject()),
                        message.getUser().getUsername());

            // Firebase
            for (UserEndpoint ep : message.getUser().getEndpoints()) {
                FirebaseMessage fireMessage = new FirebaseMessage();
                fireMessage.setRecipientToken(ep.getToken());
                fireMessage.setTitle("DWC Notification");
                fireMessage.setBody(message.getSubject());
                firebaseMessagingService.sendNotificationByToken(fireMessage);
            }
        }

        // send Email
        // determine module for id parsing
        if (message.getModule().equals(EventModule.Request)) {
            // determine if Request or Item
            if (message.getType().equals(NotificationType.New)) {
                BigInteger orderId = new BigInteger(message.getModuleId());
                OrderRequest newRequest = orderService.findById(orderId).get();
                // send Email
                EmailDetails emailDetailsSupervisor =
                        new EmailDetails(message.getUser().getContactEmail(), "A New Request", "", null);
                emailService.sendSupervisorRequest(emailDetailsSupervisor, newRequest, message.getUser().getId());
            } else if (message.getType().equals(NotificationType.NewItem)) {

            }
        }


        message.setDateSent(LocalDateTime.now());
        message.setSent(true);
        messageRepository.save(message);

        return null;
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
