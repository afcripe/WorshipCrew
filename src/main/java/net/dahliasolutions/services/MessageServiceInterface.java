package net.dahliasolutions.services;

import net.dahliasolutions.models.Message;
import net.dahliasolutions.models.Notification;

import java.util.Optional;

public interface MessageServiceInterface {

    Message createMessage(Long id, Notification notification);
    void save(Message message);
    Optional<Message> findById(Long id);
}
