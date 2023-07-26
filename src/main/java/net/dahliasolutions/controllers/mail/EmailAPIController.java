package net.dahliasolutions.controllers.mail;

import lombok.RequiredArgsConstructor;
import net.dahliasolutions.models.mail.EmailDetails;
import net.dahliasolutions.services.mail.EmailService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/mail")
@RequiredArgsConstructor
public class EmailAPIController {

    private final EmailService emailService;

    @PostMapping("/send")
    public String sendSimpleMail(@RequestBody EmailDetails emailDetails) {
        return emailService.sendSimpleMail(emailDetails);
    }
}
