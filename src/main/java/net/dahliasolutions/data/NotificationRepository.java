package net.dahliasolutions.data;

import net.dahliasolutions.models.Notification;
import net.dahliasolutions.models.NotificationModule;
import net.dahliasolutions.models.user.User;
import org.aspectj.weaver.ast.Not;
import org.springframework.data.jpa.repository.JpaRepository;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;

public interface NotificationRepository extends JpaRepository<Notification, BigInteger> {

    Optional<Notification> findByName(String name);
    List<Notification> findAllByModule(NotificationModule module);
    List<Notification> findAllByUsers(User user);
}
