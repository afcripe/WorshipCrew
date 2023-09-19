package net.dahliasolutions.controllers;

import com.google.firebase.messaging.FirebaseMessagingException;
import lombok.RequiredArgsConstructor;
import net.dahliasolutions.models.FirebaseMessage;
import net.dahliasolutions.models.FirebaseMessageModel;
import net.dahliasolutions.models.PushMessage;
import net.dahliasolutions.models.records.SingleBigIntegerModel;
import net.dahliasolutions.models.records.SingleStringModel;
import net.dahliasolutions.models.user.User;
import net.dahliasolutions.models.user.UserEndpoint;
import net.dahliasolutions.services.FirebaseMessagingService;
import net.dahliasolutions.services.PushService;
import net.dahliasolutions.services.user.EndpointService;
import net.dahliasolutions.services.user.UserService;
import org.springframework.boot.configurationprocessor.json.JSONException;
import org.springframework.boot.configurationprocessor.json.JSONObject;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/messaging")
@RequiredArgsConstructor
public class PushAPIController {

    private final PushService pushService;
    private final FirebaseMessagingService firebaseMessagingService;
    private final EndpointService endpointService;
    private final UserService userService;

    @PostMapping("/sendmessage")
    public void sendMessage(@RequestBody final PushMessage message) {

    }

    @PostMapping("/sendmessagebytoken")
    public String sendNotificationByToken(@RequestBody FirebaseMessage firebaseMessage) {
        return firebaseMessagingService.sendNotificationByToken(firebaseMessage);
    }

    @PostMapping("/sendtestfiremessage")
    public SingleStringModel sendTestByToken(@ModelAttribute FirebaseMessageModel firebaseMessage) throws FirebaseMessagingException, JSONException {
        Map<String, String> data = Map.ofEntries(
                Map.entry("link", "http://localhost:8081/app"),
                Map.entry("module", ""),
                Map.entry("moduleId", "")
                );

        FirebaseMessage msg = new FirebaseMessage();
                        msg.setRecipientToken(firebaseMessage.recipientToken());
                        msg.setTitle(firebaseMessage.title());
                        msg.setBody(firebaseMessage.body());
                        msg.setData(data);
        try {
            Thread.sleep(2000);
        } catch (Exception e) {
            //
        }

        System.out.println(msg.getTitle());
        String sentMsg = firebaseMessagingService.sendNotificationByToken(msg);
        System.out.println(sentMsg);

        return new SingleStringModel("sentMsg");
    }

    @PostMapping("/removetoken")
    public SingleStringModel removeToken(@ModelAttribute SingleBigIntegerModel bigIntegerModel) {
        Optional<UserEndpoint> endpoint = endpointService.findById(bigIntegerModel.id());
        if (endpoint.isPresent()) {
            User user = userService.findById(endpoint.get().getUser().getId()).get();
            user.getEndpoints().remove(endpoint.get());
            userService.save(user);
            endpointService.deleteById(endpoint.get().getId());
        }

        return new SingleStringModel("Deleted");
    }

}
