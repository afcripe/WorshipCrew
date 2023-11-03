package net.dahliasolutions.data;

import net.dahliasolutions.models.NotificationMessage;
import net.dahliasolutions.models.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;

public interface NotificationMessageRepository extends JpaRepository<NotificationMessage, BigInteger> {

    List<NotificationMessage> findAllByDateSentNotNull();
    List<NotificationMessage> findAllBySent(boolean sent);
    List<NotificationMessage> findAllByUser(User user);
    List<NotificationMessage> findAllByRead(boolean read);
    List<NotificationMessage> findAllByUserAndRead(User user, boolean read);
    Optional<NotificationMessage> findById(BigInteger id);
}
