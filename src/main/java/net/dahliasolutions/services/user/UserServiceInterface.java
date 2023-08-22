package net.dahliasolutions.services.user;

import net.dahliasolutions.models.campus.Campus;
import net.dahliasolutions.models.department.DepartmentCampus;
import net.dahliasolutions.models.department.DepartmentRegional;
import net.dahliasolutions.models.position.Position;
import net.dahliasolutions.models.user.User;
import net.dahliasolutions.models.user.UserRoles;
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
    List<User> searchAllByFullName(String searchTerm);
    User save(User user);
    List<User> findAll();
    List<User> findAllByCampus(Campus campus);
    List<User> findAllByCampusAndDeleted(Campus campus, boolean deleted);
    Optional<User> findByUsername(String username);
    void updateUserPosition(String username, String positionName);
    void updateUserCampus(String username, String campusName);
    void updateUserDepartment(String username, String departmentName);
    void updateUserLocationByLocationId(String username, BigInteger locationId);
    List<User> findAllByPosition(Position position);
    List<User> findAllByDepartment(DepartmentRegional department);
    List<User> findAllByDepartmentAndDeleted(DepartmentRegional department, boolean deleted);
    List<User> findAllByDepartmentCampus(DepartmentCampus department);
    List<User> findAllByActivated();
    List<User> findAllByRole(UserRoles role);
    List<User> findAllByRoles(String roles);
    List<User> findAllByDeleted(boolean deleted);

}
