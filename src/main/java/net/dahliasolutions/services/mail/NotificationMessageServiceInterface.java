package net.dahliasolutions.services.mail;

import net.dahliasolutions.models.BrowserMessage;
import net.dahliasolutions.models.NotificationMessage;
import net.dahliasolutions.models.user.User;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;

public interface NotificationMessageServiceInterface {

    NotificationMessage createMessage(NotificationMessage message);
    NotificationMessage createEventMessage(NotificationMessage message, User usr);
    BrowserMessage sendMessage(NotificationMessage message);
    List<NotificationMessage> getUnsentMessages();
    List<NotificationMessage> getUserAll(User user);
    List<NotificationMessage> getUserUnread(User user);
    List<NotificationMessage> getUserRead(User user);
    Optional<NotificationMessage> findById(BigInteger id);
    void save(NotificationMessage message);
    void sendAllMessages();
    void setMessageRead();

}
