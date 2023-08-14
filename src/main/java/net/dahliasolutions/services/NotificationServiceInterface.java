package net.dahliasolutions.services;

import net.dahliasolutions.models.Notification;
import net.dahliasolutions.models.NotificationModule;
import net.dahliasolutions.models.user.Profile;
import net.dahliasolutions.models.user.User;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;

public interface NotificationServiceInterface {

    Notification save(Notification notification);
    Optional<Notification> findById(BigInteger id);
    Optional<Notification> findByName(String name);
    List<Notification> findAll();
    List<Notification> findAllByModule(NotificationModule module);
    List<Notification> findByUser(User user);
}
