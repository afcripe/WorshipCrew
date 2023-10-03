package net.dahliasolutions.services.user;

import net.dahliasolutions.models.user.UserRoles;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;

public interface UserRolesServiceInterface {

    void createRole(String name, String description);
    void save(UserRoles role);
    Optional<UserRoles> findByName(String name);
    List<UserRoles> findAll();
    Optional<UserRoles> findById(BigInteger id);

}
