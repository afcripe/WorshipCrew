package net.dahliasolutions.services;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import lombok.RequiredArgsConstructor;
import net.dahliasolutions.models.FirebaseMessage;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FirebaseMessagingService {

    private final FirebaseMessaging firebaseMessaging;

    public String sendNotificationByToken(FirebaseMessage firebaseMessage) {
        Notification notification = Notification
                .builder()
                .setTitle(firebaseMessage.getTitle())
                .setBody(firebaseMessage.getBody())
                .setImage(firebaseMessage.getImage())
                .build();

        Message message = Message
                .builder()
                .setToken(firebaseMessage.getRecipientToken())
                .setNotification(notification)
                .putAllData(firebaseMessage.getData())
                .build();

        try {
            firebaseMessaging.send(message);
            return "Message Sent Successfully.";
        } catch (FirebaseMessagingException e) {
            e.printStackTrace();
            return "Error Sending Message.";
        }
    }

}
