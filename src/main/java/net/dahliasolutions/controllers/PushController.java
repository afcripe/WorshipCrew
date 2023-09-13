package net.dahliasolutions.controllers;

import net.dahliasolutions.models.PushMessage;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;
import org.springframework.web.util.HtmlUtils;

import java.security.Principal;

@Controller
public class PushController {

    @MessageMapping("/message")
    @SendTo("/topic/messages")
    public PushMessage getMessages(final PushMessage message){
        System.out.println(message.getTopicContent());
        return message;
    }

    @MessageMapping("/private-message")
    @SendToUser("/topic/private-messages")
    public PushMessage getPrivateMessages(final PushMessage message, final Principal principal){
        System.out.println(message.getTopicContent());
        PushMessage pushMessage = new PushMessage(
                "request",
                "1",
                "PrivateMessage: "+message.getTopicContent());
        return pushMessage;
    }
}
