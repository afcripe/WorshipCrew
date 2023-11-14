package net.dahliasolutions.controllers.mail;

import lombok.RequiredArgsConstructor;
import net.dahliasolutions.models.NotificationMessage;
import net.dahliasolutions.models.mail.EmailDetails;
import net.dahliasolutions.services.mail.EmailService;
import net.dahliasolutions.services.mail.NotificationMessageService;
import org.apache.commons.io.IOUtils;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/mail")
@RequiredArgsConstructor
public class EmailAPIController {

    private final EmailService emailService;
    private final NotificationMessageService notificationMessageService;

    @PostMapping("/send")
    public String sendSimpleMail(@RequestBody EmailDetails emailDetails) {
        return emailService.sendSimpleMail(emailDetails);
    }
}
