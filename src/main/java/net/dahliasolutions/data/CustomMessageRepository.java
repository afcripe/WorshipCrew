package net.dahliasolutions.data;

import net.dahliasolutions.models.mail.MailerCustomMessage;
import net.dahliasolutions.models.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;

public interface CustomMessageRepository extends JpaRepository<MailerCustomMessage, BigInteger> {
    Optional<MailerCustomMessage> findById(BigInteger id);
    List<MailerCustomMessage> findByUser(User user);
    List<MailerCustomMessage> findByUserAndDraft(User user, boolean draft);
}
