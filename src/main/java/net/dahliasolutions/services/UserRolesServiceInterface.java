package net.dahliasolutions.services;

import net.dahliasolutions.models.UserRoles;

import java.util.List;
import java.util.Optional;

public interface UserRolesServiceInterface {

    void createRole(String name, String description);
    Optional<UserRoles> findByName(String name);
    List<UserRoles> findAll();


}
