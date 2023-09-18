package net.dahliasolutions.services.user;

import lombok.RequiredArgsConstructor;
import net.dahliasolutions.data.UserNotificationSubscribeRepository;
import net.dahliasolutions.models.NotificationEndPoint;
import net.dahliasolutions.models.user.User;
import net.dahliasolutions.models.user.UserNotificationSubscribe;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserNotificationSubscribeService implements UserNotificationSubscribeServiceInterface{

    private final UserNotificationSubscribeRepository subscribeRepository;

    @Override
    public UserNotificationSubscribe save(UserNotificationSubscribe subscription) {
        return subscribeRepository.save(subscription);
    }

    @Override
    public List<UserNotificationSubscribe> findAllByUser(User user) {
        return subscribeRepository.findAllByUser(user);
    }

    @Override
    public Optional<UserNotificationSubscribe> findByEndPointAndUser(NotificationEndPoint endPoint, User user) {
        return subscribeRepository.findByEndPointAndUser(endPoint, user);
    }

    @Override
    public void deleteBySubscription(UserNotificationSubscribe subscription) {
        subscribeRepository.delete(subscription);
    }

    @Override
    public void deleteById(BigInteger id) {
        subscribeRepository.deleteById(id);
    }
}
