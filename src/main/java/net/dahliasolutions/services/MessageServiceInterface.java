package net.dahliasolutions.services;

import net.dahliasolutions.models.Message;
import net.dahliasolutions.models.Notification;

import java.math.BigInteger;
import java.util.Optional;

public interface MessageServiceInterface {

    Message createMessage(BigInteger id, Notification notification);
    void save(Message message);
    Optional<Message> findById(BigInteger id);
}
