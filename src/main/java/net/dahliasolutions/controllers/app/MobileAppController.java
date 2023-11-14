package net.dahliasolutions.controllers.app;

import lombok.RequiredArgsConstructor;
import net.dahliasolutions.models.NotificationMessage;
import net.dahliasolutions.models.support.Ticket;
import net.dahliasolutions.models.user.User;
import net.dahliasolutions.services.mail.NotificationMessageService;
import net.dahliasolutions.services.support.TicketService;
import net.dahliasolutions.services.user.UserService;
import org.apache.commons.io.IOUtils;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.util.List;
import java.util.Optional;

@Controller
@RequiredArgsConstructor
@RequestMapping("/api/v1/app/html")
public class MobileAppController {

    private final UserService userService;
    private final TicketService ticketService;
    private final NotificationMessageService notificationMessageService;

    @GetMapping("/listtickets")
    public String getUserTicketsHTML(Model model) {
        User currentUser = userService.findByUsername("caleb@destinyworship.com").get();
        List<Ticket> openAgentTicketList = ticketService.findAllByAgentOpenOnly(currentUser);
        model.addAttribute("agentTickets", openAgentTicketList);
        return "app/listTickets";
    }

    @GetMapping(value = "/pixel/{messageID}")
    public ResponseEntity<byte[]> getTrackingPixel (@PathVariable BigInteger messageID) throws IOException {
        Optional<NotificationMessage> message = notificationMessageService.findById(messageID);
        if (message.isPresent()) {
            message.get().setRead(true);
            notificationMessageService.save(message.get());
        }
        InputStream in = getClass().getResourceAsStream("/pixel.png");
        byte[] image = IOUtils.toByteArray(in);
        return ResponseEntity.ok().contentType(MediaType.IMAGE_PNG).body(image);
    }

}
