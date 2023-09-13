package net.dahliasolutions.services;

import lombok.AllArgsConstructor;
import net.dahliasolutions.models.PushMessage;
import org.springframework.stereotype.Service;
import org.springframework.messaging.simp.SimpMessagingTemplate;

@Service
@AllArgsConstructor
public class PushService {
    private final SimpMessagingTemplate messagingTemplate;

    public void sendPushMessageToAll(final PushMessage message) {
        messagingTemplate.convertAndSend("/topic/messages", message);
    }

    public void sendPushMessageToUser(final PushMessage message, final String username) {
        messagingTemplate.convertAndSendToUser(username, "/topic/private-messages", message);
    }
}

//{
//    "subject": "mailto: <afcripe@live.com>",
//    "publicKey": "BPkHKoGBXYuuTEfyty0lBzi1RruJbGobRImxy9Jl008QPmgNxeo7Hj2BYaDb-AJD4hOraF6ZHirFl_VtxeMKiZk",
//    "privateKey": "sH_MGjcfdAI0T9UIKS-7EYbLA9x0OyOamL443NA8sVY"
//}
