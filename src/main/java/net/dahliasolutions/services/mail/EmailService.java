package net.dahliasolutions.services.mail;

import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import net.dahliasolutions.data.MailerLinksRepository;
import net.dahliasolutions.models.*;
import net.dahliasolutions.models.mail.EmailDetails;
import net.dahliasolutions.models.mail.MailerLinks;
import net.dahliasolutions.models.order.OrderItem;
import net.dahliasolutions.models.order.OrderRequest;
import net.dahliasolutions.models.user.User;
import net.dahliasolutions.services.AuthService;
import net.dahliasolutions.services.user.UserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.File;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;

@Service@RequiredArgsConstructor
public class EmailService implements EmailServiceInterface{

    private final JavaMailSender javaMailSender;
    private final TemplateEngine templateEngine;
    private final AuthService authService;
    private final UserService userService;
    private final MailerLinksService mailerLinksService;
    private final AppServer appServer;

    @Value("${spring.mail.username}") private String sender;

    // Method 1
    // To send a simple email
    @Override
    public String sendSimpleMail(EmailDetails emailDetails) {
        try {
            SimpleMailMessage mailMessage = new SimpleMailMessage();
                mailMessage.setFrom(sender);
                mailMessage.setTo(emailDetails.getRecipient());
                mailMessage.setSubject(emailDetails.getSubject());
                mailMessage.setText(emailDetails.getMsgBody());

            javaMailSender.send(mailMessage);
            return "Mail successfully sent.";

        } catch (Exception e) {
            return "Error while Sending Mail";
        }
    }

    @Override
    public String sendSimpleMailWithAttachment(EmailDetails emailDetails) {
        MimeMessage mailMessage = javaMailSender.createMimeMessage();
        MimeMessageHelper messageHelper;

        try {
            FileSystemResource file = new FileSystemResource(new File(emailDetails.getAttachment()));

            messageHelper = new MimeMessageHelper(mailMessage, true);
                messageHelper.setFrom(sender);
                messageHelper.setTo(emailDetails.getRecipient());
                messageHelper.setSubject(emailDetails.getSubject());
                messageHelper.setText(emailDetails.getMsgBody());
                messageHelper.addAttachment(Objects.requireNonNull(file.getFilename()), file);

            javaMailSender.send(mailMessage);
            return "Mail successfully sent.";
        } catch (Exception e) {
            return "Error while Sending Mail";
        }

    }

    @Override
    public BrowserMessage sendWelcomeMail(EmailDetails emailDetails, BigInteger id) {
        String linkString = authService.randomStringGenerator(30, true, true);

        Context context = new Context();
        context.setVariable("userDetails", userService.findById(id).orElse(null));
        context.setVariable("baseURL", appServer.getBaseURL());
        context.setVariable("welcomeLink", appServer.getBaseURL()+"/mailer/"+linkString);
        context.setVariable("emailSubject", emailDetails.getSubject());

        MimeMessage message = javaMailSender.createMimeMessage();

        try {
            message.setFrom(new InternetAddress(sender));
            message.setRecipients(MimeMessage.RecipientType.TO, emailDetails.getRecipient());
            message.setSubject(emailDetails.getSubject());
            // Set the email's content to be the HTML template
            message.setContent(templateEngine.process("mailer/mailWelcome", context), "text/html; charset=utf-8");

            javaMailSender.send(message);

            MailerLinks mLink = new MailerLinks(
                    null, id, id, linkString,
                    LocalDateTime.now().plusDays(14),
                    false, "setPassword");
            mailerLinksService.save(mLink);

            return new BrowserMessage("msgSuccess", "E-mail sent.");
        } catch (Exception e) {
            return new BrowserMessage("msgError", e.getMessage());
        }
    }

    @Override
    public BrowserMessage sendPasswordResetMail(EmailDetails emailDetails, BigInteger id) {
        String linkString = authService.randomStringGenerator(30, true, true);

        Context context = new Context();
        context.setVariable("baseURL", appServer.getBaseURL());
        context.setVariable("resetLink", appServer.getBaseURL()+"/mailer/"+linkString);
        context.setVariable("emailSubject", emailDetails.getSubject());

        MimeMessage message = javaMailSender.createMimeMessage();

        try {
            message.setFrom(new InternetAddress(sender));
            message.setRecipients(MimeMessage.RecipientType.TO, emailDetails.getRecipient());
            message.setSubject(emailDetails.getSubject());
            // Set the email's content to be the HTML template
            message.setContent(templateEngine.process("mailer/mailPasswordReset", context), "text/html; charset=utf-8");

            javaMailSender.send(message);

            MailerLinks mLink = new MailerLinks(
                    null, id, id, linkString,
                    LocalDateTime.now().plusDays(14),
                    false, "resetPassword");
            mailerLinksService.save(mLink);

            return new BrowserMessage("msgSuccess", "E-mail sent.");
        } catch (Exception e) {
            return new BrowserMessage("msgError", e.getMessage());
        }
    }

    @Override
    public BrowserMessage sendUserRequest(EmailDetails emailDetails, OrderRequest orderRequest) {
        // set template variables
        Context context = new Context();
        context.setVariable("baseURL", appServer.getBaseURL());
        context.setVariable("orderRequest", orderRequest);
        context.setVariable("emailSubject", emailDetails.getSubject());

        // create mail message
        MimeMessage message = javaMailSender.createMimeMessage();

        try {
            message.setFrom(new InternetAddress(sender));
            message.setRecipients(MimeMessage.RecipientType.TO, emailDetails.getRecipient());
            message.setSubject(emailDetails.getSubject());
            // Set the email's content to be the HTML template
            message.setContent(templateEngine.process("mailer/mailUserRequest", context), "text/html; charset=utf-8");

            javaMailSender.send(message);
            return new BrowserMessage("msgSuccess", "E-mail sent.");
        } catch (Exception e) {
            return new BrowserMessage("msgError", e.getMessage());
        }
    }

    @Override
    public BrowserMessage sendSupervisorRequest(EmailDetails emailDetails, OrderRequest orderRequest, BigInteger id) {
        String linkString = authService.randomStringGenerator(30, true, true);

        // set template variables
        Context context = new Context();
        context.setVariable("baseURL", appServer.getBaseURL());
        context.setVariable("orderRequest", orderRequest);
        context.setVariable("emailSubject", emailDetails.getSubject());
        context.setVariable("webLink", appServer.getBaseURL()+"/mailer/"+linkString);

        // create mail message
        MimeMessage message = javaMailSender.createMimeMessage();

        try {
            message.setFrom(new InternetAddress(sender));
            message.setRecipients(MimeMessage.RecipientType.TO, emailDetails.getRecipient());
            message.setSubject(emailDetails.getSubject());
            // Set the email's content to be the HTML template
            message.setContent(templateEngine.process("mailer/mailSupervisorRequest", context), "text/html; charset=utf-8");

            javaMailSender.send(message);

            MailerLinks mLink = new MailerLinks(
                    null, id, orderRequest.getId(), linkString,
                    LocalDateTime.now().plusDays(5),
                    false, "acknowledgeRequest");
            mLink = mailerLinksService.createLink(mLink);

            return new BrowserMessage("msgSuccess", "E-mail sent.");
        } catch (Exception e) {
            return new BrowserMessage("msgError", e.getMessage());
        }
    }

    @Override
    public BrowserMessage sendSupervisorItemRequest(EmailDetails emailDetails, OrderItem orderItem, BigInteger id) {
        String linkString = authService.randomStringGenerator(30, true, true);

        // set template variables
        Context context = new Context();
        context.setVariable("baseURL", appServer.getBaseURL());
        context.setVariable("requestItem", orderItem);
        context.setVariable("emailSubject", emailDetails.getSubject());
        context.setVariable("webLink", appServer.getBaseURL()+"/mailer/"+linkString);

        // create mail message
        MimeMessage message = javaMailSender.createMimeMessage();

        try {
            message.setFrom(new InternetAddress(sender));
            message.setRecipients(MimeMessage.RecipientType.TO, emailDetails.getRecipient());
            message.setSubject(emailDetails.getSubject());
            // Set the email's content to be the HTML template
            message.setContent(templateEngine.process("mailer/mailItemRequest", context), "text/html; charset=utf-8");

            javaMailSender.send(message);

            MailerLinks mLink = new MailerLinks(
                    null, id, orderItem.getId(), linkString,
                    LocalDateTime.now().plusDays(5),
                    false, "acknowledgeItem");
            mailerLinksService.save(mLink);

            return new BrowserMessage("msgSuccess", "E-mail sent.");
        } catch (Exception e) {
            return new BrowserMessage("msgError", e.getMessage());
        }
    }

    @Override
    public void sendStatement(User user) {

        // set template variables
        Context context = new Context();
        context.setVariable("baseURL", appServer.getBaseURL());

        // create mail message
        MimeMessage message = javaMailSender.createMimeMessage();

        try {
            message.setFrom(new InternetAddress(sender));
            message.setRecipients(MimeMessage.RecipientType.TO, user.getContactEmail());
            message.setSubject("Volunteer Schedule");
            // Set the email's content to be the HTML template
            message.setContent(templateEngine.process("mailer/mailScheduleStatement", context), "text/html; charset=utf-8");

            javaMailSender.send(message);
        } catch (Exception e) {
            System.out.println("error sending statement");
        }
    }
}
