package net.dahliasolutions.data;

import net.dahliasolutions.models.user.Profile;
import net.dahliasolutions.models.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;

public interface ProfileRepository extends JpaRepository<Profile, BigInteger> {

    List<Profile> findAllByTheme(String name);
    Optional<Profile> findByUser(User user);
}
