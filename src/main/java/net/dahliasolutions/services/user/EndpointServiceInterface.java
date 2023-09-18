package net.dahliasolutions.services.user;

import net.dahliasolutions.models.user.User;
import net.dahliasolutions.models.user.UserEndpoint;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;

public interface EndpointServiceInterface {

    UserEndpoint save(UserEndpoint endpoint);
    Optional<UserEndpoint> findById(BigInteger id);
    List<UserEndpoint> findAllByUser(User user);
    Optional<UserEndpoint> findByToken(String token);
    void deleteByToken(String token);
    void deleteById(BigInteger id);
}
