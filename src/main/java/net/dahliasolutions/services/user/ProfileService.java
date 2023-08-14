package net.dahliasolutions.services.user;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import net.dahliasolutions.data.*;
import net.dahliasolutions.models.user.NotificationChannel;
import net.dahliasolutions.models.user.Profile;
import net.dahliasolutions.models.user.User;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class ProfileService implements ProfileServiceInterface{
    private final ProfileRepository profileRepository;

    @Override
    public Profile createDefaultProfile(User user) {
        Profile profile = new Profile();
                profile.setId(user.getId());
                profile.setUser(user);
                profile.setTheme("default");
                profile.setSideNavigation("expand");
                profile.setStoreLayout("grid");
                profile.setNotificationChannel(NotificationChannel.email);
        return profileRepository.save(profile);
    }

    @Override
    public Profile save(Profile profile) {
        return profileRepository.save(profile);
    }

    @Override
    public Optional<Profile> findByUser(User user) {
        return profileRepository.findByUser(user);
    }

    @Override
    public List<Profile> findAllByTheme(String name) {
        return profileRepository.findAllByTheme(name);
    }
}
