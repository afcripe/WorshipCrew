package net.dahliasolutions.services;

import net.dahliasolutions.models.EventType;
import net.dahliasolutions.models.Notification;
import net.dahliasolutions.models.EventModule;
import net.dahliasolutions.models.user.User;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;

public interface NotificationServiceInterface {

    Notification save(Notification notification);
    Optional<Notification> findById(BigInteger id);
    Optional<Notification> findByName(String name);
    List<Notification> findAll();
    List<Notification> findAllByModule(EventModule module);
    List<Notification> findAllByType(EventType type);
    List<Notification> findAllByModuleAndType(EventModule module, EventType type);
    List<Notification> findByUser(User user);
    void deleteById(BigInteger id);
}
