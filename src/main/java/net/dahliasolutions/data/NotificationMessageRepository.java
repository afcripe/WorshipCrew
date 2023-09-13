package net.dahliasolutions.data;

import net.dahliasolutions.models.NotificationMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.math.BigInteger;
import java.util.List;

public interface NotificationMessageRepository extends JpaRepository<NotificationMessage, BigInteger> {

    List<NotificationMessage> findAllByDateSentNotNull();
    List<NotificationMessage> findAllBySent(boolean sent);
}
