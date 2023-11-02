package net.dahliasolutions.services.mail;

import net.dahliasolutions.models.BrowserMessage;
import net.dahliasolutions.models.NotificationMessage;
import net.dahliasolutions.models.user.User;

import java.util.List;

public interface NotificationMessageServiceInterface {

    NotificationMessage createMessage(NotificationMessage message);
    NotificationMessage createEventMessage(NotificationMessage message, User usr);
    BrowserMessage sendMessage(NotificationMessage message);
    List<NotificationMessage> getUnsentMessages();
    List<NotificationMessage> getUserAll(User user);
    List<NotificationMessage> getUserUnread(User user);
    List<NotificationMessage> getUserRead(User user);
    void save(NotificationMessage message);
    void sendAllMessages();
    void setMessageRead();

}
