package net.dahliasolutions.controllers;

import lombok.RequiredArgsConstructor;
import net.dahliasolutions.models.FirebaseMessage;
import net.dahliasolutions.models.FirebaseMessageModel;
import net.dahliasolutions.models.PushMessage;
import net.dahliasolutions.models.records.SingleStringModel;
import net.dahliasolutions.services.FirebaseMessagingService;
import net.dahliasolutions.services.PushService;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/messaging")
@RequiredArgsConstructor
public class PushAPIController {

    private final PushService pushService;
    private final FirebaseMessagingService firebaseMessagingService;

    @PostMapping("/sendmessage")
    public void sendMessage(@RequestBody final PushMessage message) {

    }

    @PostMapping("/sendmessagebytoken")
    public String sendNotificationByToken(@RequestBody FirebaseMessage firebaseMessage) {
        return firebaseMessagingService.sendNotificationByToken(firebaseMessage);
    }

    @PostMapping("/sendtestfiremessage")
    public SingleStringModel sendTestByToken(@RequestBody FirebaseMessageModel firebaseMessage) {
        FirebaseMessage msg = new FirebaseMessage();
                        msg.setRecipientToken(firebaseMessage.recipientToken());
                        msg.setTitle(firebaseMessage.title());
                        msg.setBody(firebaseMessage.body());

        String sentMsg = firebaseMessagingService.sendNotificationByToken(msg);
        System.out.println(sentMsg);

        return new SingleStringModel("sentMsg");
    }

}
