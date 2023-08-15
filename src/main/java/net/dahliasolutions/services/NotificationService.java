package net.dahliasolutions.services;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import net.dahliasolutions.data.NotificationRepository;
import net.dahliasolutions.models.EventType;
import net.dahliasolutions.models.Notification;
import net.dahliasolutions.models.EventModule;
import net.dahliasolutions.models.user.User;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class NotificationService implements NotificationServiceInterface {

    private final NotificationRepository notificationRepository;


    @Override
    public Notification save(Notification notification) {
        Notification update = notificationRepository.save(notification);
        Notification verify = findById(update.getId()).get();
        return update;
    }

    @Override
    public Optional<Notification> findById(BigInteger id) {
        return notificationRepository.findById(id);
    }

    @Override
    public Optional<Notification> findByName(String name) {
        return notificationRepository.findByName(name);
    }

    @Override
    public List<Notification> findAll() {
        return notificationRepository.findAll();
    }

    @Override
    public List<Notification> findAllByModule(EventModule module) {
        return notificationRepository.findAllByModule(module);
    }

    @Override
    public List<Notification> findAllByType(EventType type) {
        return notificationRepository.findAllByType(type);
    }

    @Override
    public List<Notification> findAllByModuleAndType(EventModule module, EventType type) {
        return notificationRepository.findAllByModuleAndType(module, type);
    }

    @Override
    public List<Notification> findByUser(User user) {
        return findByUser(user);
    }

    @Override
    public void deleteById(BigInteger id) {
        notificationRepository.deleteById(id);
    }
}
