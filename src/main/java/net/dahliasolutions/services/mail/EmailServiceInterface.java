package net.dahliasolutions.services.mail;

import net.dahliasolutions.models.BrowserMessage;
import net.dahliasolutions.models.Event;
import net.dahliasolutions.models.Notification;
import net.dahliasolutions.models.mail.EmailDetails;
import net.dahliasolutions.models.order.OrderItem;
import net.dahliasolutions.models.order.OrderRequest;
import net.dahliasolutions.models.user.User;

import java.math.BigInteger;

public interface EmailServiceInterface {

    String sendSimpleMail(EmailDetails emailDetails);
    String sendSimpleMailWithAttachment(EmailDetails emailDetails);
    BrowserMessage sendWelcomeMail(EmailDetails emailDetails, BigInteger id);
    BrowserMessage sendPasswordResetMail(EmailDetails emailDetails, BigInteger id);
    BrowserMessage sendUserRequest(EmailDetails emailDetails, OrderRequest orderRequest);
    BrowserMessage sendSupervisorRequest(EmailDetails emailDetails, OrderRequest orderRequest, BigInteger id);
    BrowserMessage sendSupervisorItemRequest(EmailDetails emailDetails, OrderItem orderItem, BigInteger id);
    BrowserMessage sendSystemNotification(EmailDetails emailDetails, Event event);
    void sendStatement(User user);
}
