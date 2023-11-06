package net.dahliasolutions.services.mail;

import lombok.RequiredArgsConstructor;
import net.dahliasolutions.data.CustomMessageRepository;
import net.dahliasolutions.models.NotificationMessage;
import net.dahliasolutions.models.mail.MailerCustomMessage;
import net.dahliasolutions.models.user.User;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor

public class MailerCustomMessageService implements MailerCustomMessageServiceInterface{

    private final CustomMessageRepository customMessageRepository;

    @Override
    public MailerCustomMessage createMessage(MailerCustomMessage message) {
        return customMessageRepository.save(message);
    }

    @Override
    public void save(MailerCustomMessage message) {
        customMessageRepository.save(message);
    }

    @Override
    public Optional<MailerCustomMessage> findById(BigInteger id) {
        return customMessageRepository.findById(id);
    }

    @Override
    public List<MailerCustomMessage> findAll() {
        return customMessageRepository.findAll();
    }

    @Override
    public List<MailerCustomMessage> findAllByUser(User user) {
        return customMessageRepository.findByUser(user);
    }

    @Override
    public List<MailerCustomMessage> findByUserAndDraft(User user, boolean draft) {
        return customMessageRepository.findByUserAndDraft(user, draft);
    }
}
