package net.dahliasolutions.services.mail;

import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import net.dahliasolutions.models.*;
import net.dahliasolutions.models.mail.EmailDetails;
import net.dahliasolutions.models.mail.MailerCustomMessage;
import net.dahliasolutions.models.mail.MailerLinks;
import net.dahliasolutions.models.order.OrderItem;
import net.dahliasolutions.models.order.OrderNote;
import net.dahliasolutions.models.order.OrderRequest;
import net.dahliasolutions.models.support.Ticket;
import net.dahliasolutions.models.support.TicketNote;
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

@Service
@RequiredArgsConstructor
public class EmailService implements EmailServiceInterface{

    private final JavaMailSender javaMailSender;
    private final TemplateEngine templateEngine;
    private final AuthService authService;
    private final UserService userService;
    private final MailerLinksService mailerLinksService;
    private final MailerCustomMessageService customMessageService;
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
        context.setVariable("messageId", emailDetails.getMessageId());
        context.setVariable("baseURL", appServer.getBaseURL());
        context.setVariable("welcomeLink", appServer.getBaseURL()+"/mailer/"+linkString);
        context.setVariable("emailSubject", emailDetails.getSubject());

        MimeMessage message = javaMailSender.createMimeMessage();

        try {
            message.setFrom(new InternetAddress(sender));
            message.setRecipients(MimeMessage.RecipientType.TO, emailDetails.getRecipient());
            message.setSubject("!DO NOT REPLY! "+emailDetails.getSubject());
            // Set the email's content to be the HTML template
            message.setContent(templateEngine.process("mailer/mailWelcome", context), "text/html; charset=utf-8");

            javaMailSender.send(message);

            MailerLinks mLink = new MailerLinks(
                    null, id, id, "", linkString,
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
        context.setVariable("messageId", emailDetails.getMessageId());
        context.setVariable("resetLink", appServer.getBaseURL()+"/mailer/"+linkString);
        context.setVariable("emailSubject", emailDetails.getSubject());
        context.setVariable("userId", id);

        MimeMessage message = javaMailSender.createMimeMessage();

        try {
            message.setFrom(new InternetAddress(sender));
            message.setRecipients(MimeMessage.RecipientType.TO, emailDetails.getRecipient());
            message.setSubject("!DO NOT REPLY! "+emailDetails.getSubject());
            // Set the email's content to be the HTML template
            message.setContent(templateEngine.process("mailer/mailPasswordReset", context), "text/html; charset=utf-8");

            javaMailSender.send(message);

            MailerLinks mLink = new MailerLinks(
                    null, id, id, "", linkString,
                    LocalDateTime.now().plusDays(14),
                    false, "resetPassword");
            mailerLinksService.save(mLink);

            return new BrowserMessage("msgSuccess", "E-mail sent.");
        } catch (Exception e) {
            return new BrowserMessage("msgError", e.getMessage());
        }
    }

    @Override
    public BrowserMessage sendUserRequest(EmailDetails emailDetails, OrderRequest orderRequest, OrderNote orderNote) {
        // set template variables
        Context context = new Context();
        context.setVariable("baseURL", appServer.getBaseURL());
        context.setVariable("messageId", emailDetails.getMessageId());
        context.setVariable("orderRequest", orderRequest);
        context.setVariable("orderNote", orderNote);
        context.setVariable("emailSubject", emailDetails.getSubject());

        // create mail message
        MimeMessage message = javaMailSender.createMimeMessage();

        try {
            message.setFrom(new InternetAddress(sender));
            message.setRecipients(MimeMessage.RecipientType.TO, emailDetails.getRecipient());
            message.setSubject("!DO NOT REPLY! "+emailDetails.getSubject());
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
        context.setVariable("messageId", emailDetails.getMessageId());
        context.setVariable("orderRequest", orderRequest);
        context.setVariable("emailSubject", emailDetails.getSubject());
        context.setVariable("webLink", appServer.getBaseURL()+"/mailer/"+linkString);

        // create mail message
        MimeMessage message = javaMailSender.createMimeMessage();

        try {
            message.setFrom(new InternetAddress(sender));
            message.setRecipients(MimeMessage.RecipientType.TO, emailDetails.getRecipient());
            message.setSubject("!DO NOT REPLY! "+emailDetails.getSubject());
            // Set the email's content to be the HTML template
            message.setContent(templateEngine.process("mailer/mailSupervisorRequest", context), "text/html; charset=utf-8");

            javaMailSender.send(message);

            MailerLinks mLink = new MailerLinks(
                    null, id, orderRequest.getId(), "", linkString,
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
        context.setVariable("messageId", emailDetails.getMessageId());
        context.setVariable("requestItem", orderItem);
        context.setVariable("emailSubject", emailDetails.getSubject());
        context.setVariable("webLink", appServer.getBaseURL()+"/mailer/"+linkString);

        // create mail message
        MimeMessage message = javaMailSender.createMimeMessage();

        try {
            message.setFrom(new InternetAddress(sender));
            message.setRecipients(MimeMessage.RecipientType.TO, emailDetails.getRecipient());
            message.setSubject("!DO NOT REPLY! "+emailDetails.getSubject());
            // Set the email's content to be the HTML template
            message.setContent(templateEngine.process("mailer/mailItemRequest", context), "text/html; charset=utf-8");

            javaMailSender.send(message);

            MailerLinks mLink = new MailerLinks(
                    null, id, orderItem.getId(), "", linkString,
                    LocalDateTime.now().plusDays(5),
                    false, "acknowledgeItem");
            mailerLinksService.save(mLink);

            return new BrowserMessage("msgSuccess", "E-mail sent.");
        } catch (Exception e) {
            return new BrowserMessage("msgError", e.getMessage());
        }
    }

    @Override
    public BrowserMessage sendItemUpdate(EmailDetails emailDetails, OrderItem orderItem, OrderNote orderNote) {

        // set template variables
        Context context = new Context();
        context.setVariable("baseURL", appServer.getBaseURL());
        context.setVariable("messageId", emailDetails.getMessageId());
        context.setVariable("requestItem", orderItem);
        context.setVariable("orderNote", orderNote);
        context.setVariable("emailSubject", emailDetails.getSubject());

        // create mail message
        MimeMessage message = javaMailSender.createMimeMessage();

        try {
            message.setFrom(new InternetAddress(sender));
            message.setRecipients(MimeMessage.RecipientType.TO, emailDetails.getRecipient());
            message.setSubject("!DO NOT REPLY! "+emailDetails.getSubject());
            // Set the email's content to be the HTML template
            message.setContent(templateEngine.process("mailer/mailItemUpdate", context), "text/html; charset=utf-8");

            javaMailSender.send(message);

            return new BrowserMessage("msgSuccess", "E-mail sent.");
        } catch (Exception e) {
            return new BrowserMessage("msgError", e.getMessage());
        }
    }

    @Override
    public BrowserMessage sendUserTicket(EmailDetails emailDetails, Ticket ticket, TicketNote ticketNote) {
        // set template variables
        Context context = new Context();
        context.setVariable("baseURL", appServer.getBaseURL());
        context.setVariable("messageId", emailDetails.getMessageId());
        context.setVariable("ticket", ticket);
        context.setVariable("ticketNote", ticketNote);
        context.setVariable("emailSubject", emailDetails.getSubject());

        // create mail message
        MimeMessage message = javaMailSender.createMimeMessage();

        try {
            message.setFrom(new InternetAddress(sender));
            message.setRecipients(MimeMessage.RecipientType.TO, emailDetails.getRecipient());
            message.setSubject("!DO NOT REPLY! "+emailDetails.getSubject());
            // Set the email's content to be the HTML template
            message.setContent(templateEngine.process("mailer/mailUserTicket", context), "text/html; charset=utf-8");

            javaMailSender.send(message);
            return new BrowserMessage("msgSuccess", "E-mail sent.");
        } catch (Exception e) {
            return new BrowserMessage("msgError", e.getMessage());
        }
    }

    @Override
    public BrowserMessage sendAgentTicket(EmailDetails emailDetails, Ticket ticket, TicketNote ticketNote, BigInteger id) {
        String linkString = authService.randomStringGenerator(30, true, true);

        // set template variables
        Context context = new Context();
        context.setVariable("baseURL", appServer.getBaseURL());
        context.setVariable("messageId", emailDetails.getMessageId());
        context.setVariable("ticket", ticket);
        context.setVariable("ticketNote", ticketNote);
        context.setVariable("emailSubject", emailDetails.getSubject());
        context.setVariable("webLink", appServer.getBaseURL()+"/mailer/"+linkString);

        // create mail message
        MimeMessage message = javaMailSender.createMimeMessage();

        try {
            message.setFrom(new InternetAddress(sender));
            message.setRecipients(MimeMessage.RecipientType.TO, emailDetails.getRecipient());
            message.setSubject("!DO NOT REPLY! "+emailDetails.getSubject());
            // Set the email's content to be the HTML template
            message.setContent(templateEngine.process("mailer/mailAgentTicket", context), "text/html; charset=utf-8");

            javaMailSender.send(message);

            MailerLinks mLink = new MailerLinks(
                    null, id, BigInteger.valueOf(0), ticket.getId(), linkString,
                    LocalDateTime.now().plusDays(5),
                    false, "acknowledgeTicket");
            mLink = mailerLinksService.createLink(mLink);

            return new BrowserMessage("msgSuccess", "E-mail sent.");
        } catch (Exception e) {
            return new BrowserMessage("msgError", e.getMessage());
        }
    }

    @Override
    public BrowserMessage sendAgentListTicket(EmailDetails emailDetails, Ticket ticket, TicketNote ticketNote, BigInteger id) {
        String linkString = authService.randomStringGenerator(30, true, true);

        // set template variables
        Context context = new Context();
        context.setVariable("baseURL", appServer.getBaseURL());
        context.setVariable("messageId", emailDetails.getMessageId());
        context.setVariable("ticket", ticket);
        context.setVariable("ticketNote", ticketNote);
        context.setVariable("agentList", ticket.getAgentList());
        context.setVariable("emailSubject", emailDetails.getSubject());
        context.setVariable("webLink", appServer.getBaseURL()+"/mailer/"+linkString);

        // create mail message
        MimeMessage message = javaMailSender.createMimeMessage();

        try {
            message.setFrom(new InternetAddress(sender));
            message.setRecipients(MimeMessage.RecipientType.TO, emailDetails.getRecipient());
            message.setSubject("!DO NOT REPLY! "+emailDetails.getSubject());
            // Set the email's content to be the HTML template
            message.setContent(templateEngine.process("mailer/mailAgentListTicket", context), "text/html; charset=utf-8");

            javaMailSender.send(message);

            MailerLinks mLink = new MailerLinks(
                    null, id, BigInteger.valueOf(0), ticket.getId(), linkString,
                    LocalDateTime.now().plusDays(5),
                    false, "agentAcceptTicket");
            mLink = mailerLinksService.createLink(mLink);

            return new BrowserMessage("msgSuccess", "E-mail sent.");
        } catch (Exception e) {
            return new BrowserMessage("msgError", e.getMessage());
        }
    }

    @Override
    public BrowserMessage sendUserUpdateTicket(EmailDetails emailDetails, Ticket ticket, TicketNote ticketNote) {

        // set template variables
        Context context = new Context();
        context.setVariable("baseURL", appServer.getBaseURL());
        context.setVariable("messageId", emailDetails.getMessageId());
        context.setVariable("ticket", ticket);
        context.setVariable("ticketNote", ticketNote);
        context.setVariable("emailSubject", emailDetails.getSubject());

        // create mail message
        MimeMessage message = javaMailSender.createMimeMessage();

        try {
            message.setFrom(new InternetAddress(sender));
            message.setRecipients(MimeMessage.RecipientType.TO, emailDetails.getRecipient());
            message.setSubject("!DO NOT REPLY! "+emailDetails.getSubject());
            // Set the email's content to be the HTML template
            message.setContent(templateEngine.process("mailer/mailUserUpdateTicket", context), "text/html; charset=utf-8");

            javaMailSender.send(message);

            return new BrowserMessage("msgSuccess", "E-mail sent.");
        } catch (Exception e) {
            return new BrowserMessage("msgError", e.getMessage());
        }
    }

    @Override
    public BrowserMessage sendAgentUpdateTicket(EmailDetails emailDetails, Ticket ticket, TicketNote ticketNote) {

        // set template variables
        Context context = new Context();
        context.setVariable("baseURL", appServer.getBaseURL());
        context.setVariable("messageId", emailDetails.getMessageId());
        context.setVariable("ticket", ticket);
        context.setVariable("ticketNote", ticketNote);
        context.setVariable("emailSubject", emailDetails.getSubject());

        // create mail message
        MimeMessage message = javaMailSender.createMimeMessage();

        try {
            message.setFrom(new InternetAddress(sender));
            message.setRecipients(MimeMessage.RecipientType.TO, emailDetails.getRecipient());
            message.setSubject("!DO NOT REPLY! "+emailDetails.getSubject());
            // Set the email's content to be the HTML template
            message.setContent(templateEngine.process("mailer/mailAgentUpdateTicket", context), "text/html; charset=utf-8");

            javaMailSender.send(message);

            return new BrowserMessage("msgSuccess", "E-mail sent.");
        } catch (Exception e) {
            return new BrowserMessage("msgError", e.getMessage());
        }
    }

    @Override
    public BrowserMessage sendSystemNotification(EmailDetails emailDetails, AppEvent event) {
        // set template variables
        Context context = new Context();
        context.setVariable("baseURL", appServer.getBaseURL());
        context.setVariable("messageId", emailDetails.getMessageId());
        context.setVariable("notification", event);
        context.setVariable("emailSubject", emailDetails.getSubject());

        // create mail message
        MimeMessage message = javaMailSender.createMimeMessage();

        try {
            message.setFrom(new InternetAddress(sender));
            message.setRecipients(MimeMessage.RecipientType.TO, emailDetails.getRecipient());
            message.setSubject("!DO NOT REPLY! "+emailDetails.getSubject());
            // Set the email's content to be the HTML template
            message.setContent(templateEngine.process("mailer/mailNotification", context), "text/html; charset=utf-8");

            javaMailSender.send(message);
            return new BrowserMessage("msgSuccess", "E-mail sent.");
        } catch (Exception e) {
            return new BrowserMessage("msgError", e.getMessage());
        }
    }

    @Override
    public BrowserMessage sendCustomMessage(EmailDetails emailDetails, BigInteger messageId) {
        // get message
        Optional<MailerCustomMessage> customMessage = customMessageService.findById(messageId);
        if (customMessage.isPresent()) {
            // set template variables
            Context context = new Context();
            context.setVariable("baseURL", appServer.getBaseURL());
            context.setVariable("messageId", emailDetails.getMessageId());
            context.setVariable("notification", customMessage.get());

            // create mail message
            MimeMessage message = javaMailSender.createMimeMessage();

            try {
                message.setFrom(new InternetAddress(sender));
                message.setRecipients(MimeMessage.RecipientType.TO, emailDetails.getRecipient());
                message.setSubject("!DO NOT REPLY! "+emailDetails.getSubject());
                // Set the email's content to be the HTML template
                message.setContent(templateEngine.process("mailer/mailCustomNotification", context), "text/html; charset=utf-8");

                javaMailSender.send(message);
                return new BrowserMessage("msgSuccess", "E-mail sent.");
            } catch (Exception e) {
                return new BrowserMessage("msgError", e.getMessage());
            }
        } else {
            return new BrowserMessage("msgError", "No message found!");
        }
    }

}
