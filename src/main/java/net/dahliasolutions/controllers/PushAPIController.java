package net.dahliasolutions.controllers;

import lombok.RequiredArgsConstructor;
import net.dahliasolutions.models.FirebaseMessage;
import net.dahliasolutions.models.PushMessage;
import net.dahliasolutions.services.FirebaseMessagingService;
import net.dahliasolutions.services.PushService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/messaging")
@RequiredArgsConstructor
public class PushAPIController {

    private final PushService pushService;
    private final FirebaseMessagingService firebaseMessagingService;

    @PostMapping("/sendmessage")
    public void sendMessage(@RequestBody final PushMessage message) {

    }

    @PostMapping
    public String sendNotificationByToken(@RequestBody FirebaseMessage firebaseMessage) {
        return firebaseMessagingService.sendNotificationByToken(firebaseMessage);
    }

}
