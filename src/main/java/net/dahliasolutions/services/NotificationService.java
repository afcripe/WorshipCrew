package net.dahliasolutions.services;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import net.dahliasolutions.data.NotificationRepository;
import net.dahliasolutions.models.Notification;
import net.dahliasolutions.models.NotificationModule;
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
        return notificationRepository.save(notification);
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
    public List<Notification> findAllByModule(NotificationModule module) {
        return notificationRepository.findAllByModule(module);
    }

    @Override
    public List<Notification> findByUser(User user) {
        return findByUser(user);
    }
}
