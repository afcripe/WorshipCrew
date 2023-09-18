package net.dahliasolutions.services.user;


import net.dahliasolutions.models.NotificationEndPoint;
import net.dahliasolutions.models.user.User;
import net.dahliasolutions.models.user.UserNotificationSubscribe;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;

public interface UserNotificationSubscribeServiceInterface {

    UserNotificationSubscribe save(UserNotificationSubscribe subscription);
    List<UserNotificationSubscribe> findAllByUser(User user);
    Optional<UserNotificationSubscribe> findByEndPointAndUser(NotificationEndPoint endPoint, User user);
    void deleteBySubscription(UserNotificationSubscribe subscription);
    void deleteById(BigInteger id);

}
