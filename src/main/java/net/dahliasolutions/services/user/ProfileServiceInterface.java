package net.dahliasolutions.services.user;

import net.dahliasolutions.models.campus.Campus;
import net.dahliasolutions.models.department.DepartmentCampus;
import net.dahliasolutions.models.position.Position;
import net.dahliasolutions.models.user.Profile;
import net.dahliasolutions.models.user.User;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;

public interface ProfileServiceInterface {

    Profile createDefaultProfile(User user);
    Profile save(Profile profile);
    Optional<Profile> findByUser(User user);
    List<Profile> findAllByTheme(String name);
}
