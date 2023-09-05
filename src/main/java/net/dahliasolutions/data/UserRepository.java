package net.dahliasolutions.data;

import net.dahliasolutions.models.campus.Campus;
import net.dahliasolutions.models.department.DepartmentCampus;
import net.dahliasolutions.models.position.Position;
import net.dahliasolutions.models.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, BigInteger> {

    Optional<User> findByUsernameIgnoreCase(String username);
    boolean existsByUsernameIgnoreCase(String username);
    List<User> findAllByPosition(Position position);
    List<User> findAllByPositionAndDeleted(Position position, boolean deleted);
    List<User> findAllByCampus(Campus campus);
    List<User> findAllByCampusAndDeleted(Campus campus, boolean deleted);
    List<User> findAllByDepartmentAndDeleted(DepartmentCampus department, boolean deleted);
    List<User> findAllByActivated(boolean activated);
    List<User> findAllByDeleted(boolean deleted);

    @Query(value="SELECT * FROM USER_DETAILS WHERE CONCAT(UPPER(FIRST_NAME), ' ', UPPER(LAST_NAME)) LIKE CONCAT('%',UPPER(:searchTerm),'%')", nativeQuery = true)
    List<User> searchAllByFullName(@Param("searchTerm") String searchTerm);

    @Modifying
    @Query(value="DELETE FROM USER_DETAILS_USER_ROLES WHERE USER_ID=:userId AND USER_ROLES_ID=:roleId", nativeQuery = true)
    void deleteRoleFromUser(@Param("userId") BigInteger userId, @Param("roleId") BigInteger roleId);
}
