package net.dahliasolutions.services;

import lombok.RequiredArgsConstructor;
import net.dahliasolutions.data.MessageRepository;
import net.dahliasolutions.models.Message;
import net.dahliasolutions.models.Notification;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MessageService implements MessageServiceInterface{

    private final MessageRepository messageRepository;

    @Override
    public Message createMessage(BigInteger id, Notification notification) {
        Message message = new Message();
            message.setId(id);
            message.setName(notification.getName());
            message.setDescription(notification.getDescription());
            message.setModule(notification.getModule());
            message.setType(notification.getType());
            message.setNotification(notification);
            message.setUsers(notification.getUsers());
        return message;
    }

    @Override
    public void save(Message message) {
        messageRepository.save(message);
    }

    @Override
    public Optional<Message> findById(BigInteger id) {
        return messageRepository.findById(id);
    }
}
