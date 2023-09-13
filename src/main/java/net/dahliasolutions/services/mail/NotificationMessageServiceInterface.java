package net.dahliasolutions.services.mail;

import net.dahliasolutions.models.BrowserMessage;
import net.dahliasolutions.models.NotificationMessage;

import java.util.List;

public interface NotificationMessageServiceInterface {

    NotificationMessage createMessage(NotificationMessage message);
    BrowserMessage sendMessage(NotificationMessage message);
    List<NotificationMessage> getUnsentMessages();
    void save(NotificationMessage message);
    void sendAllMessages();

}
