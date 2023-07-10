package net.dahliasolutions.data;

import net.dahliasolutions.models.UserRoles;
import org.springframework.data.jpa.repository.JpaRepository;

import java.math.BigInteger;
import java.util.Optional;

public interface UserRolesRepository extends JpaRepository<UserRoles, BigInteger> {
    Optional<UserRoles> findByName(String name);
}
