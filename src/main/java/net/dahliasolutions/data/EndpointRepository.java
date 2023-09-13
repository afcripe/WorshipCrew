package net.dahliasolutions.data;

import net.dahliasolutions.models.user.User;
import net.dahliasolutions.models.user.UserEndpoint;
import org.springframework.data.jpa.repository.JpaRepository;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;

public interface EndpointRepository extends JpaRepository<UserEndpoint, BigInteger> {

    List<UserEndpoint> findAllByUser(User user);
    Optional<UserEndpoint> findByToken(String token);
    void deleteByToken(String token);
}
