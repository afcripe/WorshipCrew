package net.dahliasolutions.services.mail;

import net.dahliasolutions.models.NotificationMessage;
import net.dahliasolutions.models.mail.MailerCustomMessage;
import net.dahliasolutions.models.mail.MailerCustomMessageModel;
import net.dahliasolutions.models.user.User;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;

public interface MailerCustomMessageServiceInterface {

    MailerCustomMessage save(MailerCustomMessage message);
    Optional<MailerCustomMessage> findById(BigInteger id);
    List<MailerCustomMessage> findAll();
    List<MailerCustomMessage> findAllByUser(User user);
    List<MailerCustomMessage> findByUserAndDraft(User user, boolean draft);
    MailerCustomMessageModel convertEntityToModel(MailerCustomMessage entity);
    MailerCustomMessage convertModelToEntity(MailerCustomMessageModel model);
    void deleteById(BigInteger id);
}
