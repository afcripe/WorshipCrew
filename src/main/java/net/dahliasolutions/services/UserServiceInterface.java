package net.dahliasolutions.services;

import net.dahliasolutions.models.Campus;
import net.dahliasolutions.models.Position;
import net.dahliasolutions.models.User;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;

public interface UserServiceInterface extends UserDetailsService {

    User createDefaultUser(User user);
    User createUser(User user);
    boolean verifyByUsername(String username);
    void updateUserPasswordById(BigInteger id ,String password);
    void updateActivateUserPasswordById(BigInteger id ,String password);
    void addRoleToUser(String username ,String roleName);
    void removeRoleFromUser(BigInteger id ,String roleName);
    Optional<User> findById(BigInteger id);
    User save(User user);
    List<User> findAll();
    Optional<User> findByUsername(String username);
    void updateUserPosition(String username, String positionName);
    void updateUserLocation(String username, String locationName);
    void updateUserLocationByLocationId(String username, BigInteger locationId);
    List<User> findAllByPosition(Position position);
    List<User> findAllByLocation(Campus location);
    List<User> findAllByActivated();

}
