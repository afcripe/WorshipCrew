package net.dahliasolutions.services.mail;

import net.dahliasolutions.models.AppEvent;
import net.dahliasolutions.models.BrowserMessage;
import net.dahliasolutions.models.mail.EmailDetails;
import net.dahliasolutions.models.order.OrderItem;
import net.dahliasolutions.models.order.OrderNote;
import net.dahliasolutions.models.order.OrderRequest;
import net.dahliasolutions.models.support.Ticket;
import net.dahliasolutions.models.support.TicketNote;
import net.dahliasolutions.models.user.User;

import java.math.BigInteger;

public interface EmailServiceInterface {

    String sendSimpleMail(EmailDetails emailDetails);
    String sendSimpleMailWithAttachment(EmailDetails emailDetails);
    BrowserMessage sendWelcomeMail(EmailDetails emailDetails, BigInteger id);
    BrowserMessage sendPasswordResetMail(EmailDetails emailDetails, BigInteger id);
    BrowserMessage sendUserRequest(EmailDetails emailDetails, OrderRequest orderRequest, OrderNote orderNote);
    BrowserMessage sendSupervisorRequest(EmailDetails emailDetails, OrderRequest orderRequest, BigInteger id);
    BrowserMessage sendSupervisorItemRequest(EmailDetails emailDetails, OrderItem orderItem, BigInteger id);
    BrowserMessage sendItemUpdate(EmailDetails emailDetails, OrderItem orderItem, OrderNote orderNote);
    BrowserMessage sendUserTicket(EmailDetails emailDetails, Ticket ticket, TicketNote ticketNote);
    BrowserMessage sendAgentTicket(EmailDetails emailDetails, Ticket ticket, TicketNote ticketNote, BigInteger id);
    BrowserMessage sendAgentListTicket(EmailDetails emailDetails, Ticket ticket, TicketNote ticketNote, BigInteger id);
    BrowserMessage sendUserUpdateTicket(EmailDetails emailDetails, Ticket ticket, TicketNote ticketNote);
    BrowserMessage sendAgentUpdateTicket(EmailDetails emailDetails, Ticket ticket, TicketNote ticketNote);
    BrowserMessage sendSystemNotification(EmailDetails emailDetails, AppEvent event);
    BrowserMessage sendCustomMessage(EmailDetails emailDetails, BigInteger messageId);
}
