package net.dahliasolutions.data;

import net.dahliasolutions.models.EventType;
import net.dahliasolutions.models.Notification;
import net.dahliasolutions.models.EventModule;
import net.dahliasolutions.models.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;

public interface NotificationRepository extends JpaRepository<Notification, BigInteger> {

    Optional<Notification> findByName(String name);
    List<Notification> findAllByModule(EventModule module);
    List<Notification> findAllByType(EventType type);
    List<Notification> findAllByModuleAndType(EventModule module, EventType type);
    List<Notification> findAllByUsers(User user);
}
