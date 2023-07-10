package net.dahliasolutions.data;

import net.dahliasolutions.models.Campus;
import net.dahliasolutions.models.Department;
import net.dahliasolutions.models.Position;
import net.dahliasolutions.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, BigInteger> {

    Optional<User> findByUsername(String username);
    boolean existsByUsername(String username);
    List<User> findAllByPosition(Position position);
    List<User> findAllByCampus(Campus campus);
    List<User> findAllByDepartment(Department department);

    List<User> findAllByActivated(boolean activated);

    @Modifying
    @Query(value="DELETE FROM USER_DETAILS_USER_ROLES WHERE USER_ID=:userId AND USER_ROLES_ID=:roleId", nativeQuery = true)
    void deleteRoleFromUser(@Param("userId") BigInteger userId, @Param("roleId") BigInteger roleId);
}
