package net.dahliasolutions.services;

import net.dahliasolutions.models.BrowserMessage;
import net.dahliasolutions.models.EmailDetails;
import net.dahliasolutions.models.User;

import java.math.BigInteger;

public interface EmailServiceInterface {

    String sendSimpleMail(EmailDetails emailDetails);
    String sendSimpleMailWithAttachment(EmailDetails emailDetails);
    BrowserMessage sendRequestMail(EmailDetails emailDetails, BigInteger id);
    BrowserMessage sendWelcomeMail(EmailDetails emailDetails, BigInteger id);
    BrowserMessage sendPasswordResetMail(EmailDetails emailDetails, BigInteger id);
    void sendStatement(User user);
}
