package net.dahliasolutions.data;

import net.dahliasolutions.models.NotificationEndPoint;
import net.dahliasolutions.models.user.User;
import net.dahliasolutions.models.user.UserNotificationSubscribe;
import org.springframework.data.jpa.repository.JpaRepository;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;

public interface UserNotificationSubscribeRepository extends JpaRepository<UserNotificationSubscribe, BigInteger> {

    List<UserNotificationSubscribe> findAllByUser(User user);
    Optional<UserNotificationSubscribe> findByEndPointAndUser(NotificationEndPoint endPoint, User user);

}
