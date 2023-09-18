package net.dahliasolutions.services.user;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import net.dahliasolutions.data.*;
import net.dahliasolutions.models.NotificationEndPoint;
import net.dahliasolutions.models.campus.Campus;
import net.dahliasolutions.models.department.DepartmentCampus;
import net.dahliasolutions.models.department.DepartmentRegional;
import net.dahliasolutions.models.position.Position;
import net.dahliasolutions.models.user.User;
import net.dahliasolutions.models.user.UserNotificationSubscribe;
import net.dahliasolutions.models.user.UserRoles;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class UserService implements UserServiceInterface{

    private final UserRepository userRepository;
    private final UserRolesRepository rolesRepository;
    private final PositionRepository positionRepository;
    private final PasswordEncoder passwordEncoder;
    private final CampusRepository campusRepository;
    private final DepartmentCampusRepository departmentCampusRepository;
    private final ProfileService profileService;
    private final UserNotificationSubscribeRepository subscribeRepository;

    @Override
    public User createDefaultUser(User user) {
        return setNewUser(user, true);
    }

    @Override
    public User createUser(User user) {
        return setNewUser(user, false);
    }

    @Override
    public boolean verifyByUsername(String username) {
        return userRepository.existsByUsernameIgnoreCase(username);
    }

    @Override
    public void updateUserPasswordById(BigInteger id, String password) {
        User user = userRepository.findById(id).orElse(null);
        if (user != null) {
            user.setPassword(passwordEncoder.encode(password));
            userRepository.save(user);
        }
    }

    @Override
    public void updateActivateUserPasswordById(BigInteger id, String password) {
        User user = userRepository.findById(id).orElse(null);
        if (user != null) {
            user.setPassword(passwordEncoder.encode(password));
            user.setActivated(true);
            userRepository.save(user);
        }
    }

    @Override
    public void addRoleToUser(String username, String roleName) {
        Optional<User> user = userRepository.findByUsernameIgnoreCase(username);
        Optional<UserRoles> role = rolesRepository.findByName(roleName);
        if (user.isPresent() && role.isPresent()) {
            user.get().getUserRoles().add(role.get());
            userRepository.save(user.get());
        }

    }

    @Override
    public void removeRoleFromUser(BigInteger id, String roleName) {
        Optional<User> user = userRepository.findById(id);
        if (user.isPresent()) {
            for (UserRoles role : user.get().getUserRoles()) {
                if (role.getName().equals(roleName)) {
                    userRepository.deleteRoleFromUser(id, role.getId());
                }
            }
        }
    }

    @Override
    public Optional<User> findById(BigInteger id) {
        return userRepository.findById(id);
    }

    @Override
    public List<User> searchAllByFullName(String searchTerm) {
        return userRepository.searchAllByFullName(searchTerm);
    }

    @Override
    public User save(User user) {
        return userRepository.save(user);
    }

    @Override
    public List<User> findAll() {
        return userRepository.findAllByDeleted(false);
    }

    @Override
    public List<User> findAllByCampus(Campus campus) {
        return userRepository.findAllByCampusAndDeleted(campus, false);
    }

    @Override
    public List<User> findAllByCampusAndDeleted(Campus campus, boolean deleted) {
        return userRepository.findAllByCampusAndDeleted(campus, deleted);
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsernameIgnoreCase(username);
    }

    @Override
    public void updateUserPosition(String username, String positionName) {
        Optional<User> user = userRepository.findByUsernameIgnoreCase(username);
        Optional<Position> position = positionRepository.findByName(positionName);
        if (user.isPresent() && position.isPresent()) {
            user.get().setPosition(position.get());
            userRepository.save(user.get());
        }
    }

    @Override
    public void updateUserCampus(String username, String campusName) {
        Optional<User> user = userRepository.findByUsernameIgnoreCase(username);
        Optional<Campus> campus = campusRepository.findByName(campusName);
        if (user.isPresent() && campus.isPresent()) {
            user.get().setCampus(campus.get());
            userRepository.save(user.get());
        }
    }

    @Override
    public void updateUserDepartment(String username, String departmentName) {
        Optional<User> user = userRepository.findByUsernameIgnoreCase(username);
        Optional<DepartmentCampus> department = departmentCampusRepository.findByNameAndCampus(departmentName, user.get().getCampus());
        if (user.isPresent() && department.isPresent()) {
            user.get().setDepartment(department.get());
            userRepository.save(user.get());
        }
    }

    @Override
    public void updateUserLocationByLocationId(String username, BigInteger locationId) {
        Optional<User> user = userRepository.findByUsernameIgnoreCase(username);
        Optional<Campus> campus = campusRepository.findById(locationId);
        if (user.isPresent() && campus.isPresent()) {
            user.get().setCampus(campus.get());
            userRepository.save(user.get());
        }
    }

    @Override
    public List<User> findAllByPosition(Position position) {
        return userRepository.findAllByPositionAndDeleted(position, false);
    }

    @Override
    public List<User> findAllByDepartment(DepartmentRegional department) {
        List<User> userList = new ArrayList<>();
        List<DepartmentCampus> campusDepartmentList = departmentCampusRepository.findAllByRegionalDepartment(department);
        for (DepartmentCampus departmentCampus : campusDepartmentList) {
            List<User> departmentUsers = userRepository.findAllByDepartmentAndDeleted(departmentCampus, false);
            userList.addAll(departmentUsers);
        }
        return userList;
    }

    @Override
    public List<User> findAllByDepartmentAndDeleted(DepartmentRegional department, boolean deleted) {
        List<User> userList = new ArrayList<>();
        List<DepartmentCampus> campusDepartmentList = departmentCampusRepository.findAllByRegionalDepartment(department);
        for (DepartmentCampus departmentCampus : campusDepartmentList) {
            List<User> departmentUsers = userRepository.findAllByDepartmentAndDeleted(departmentCampus, deleted);
            userList.addAll(departmentUsers);
        }
        return userList;
    }

    @Override
    public List<User> findAllByDepartmentCampus(DepartmentCampus department) {
        return userRepository.findAllByDepartmentAndDeleted(department, false);
    }

    @Override
    public List<User> findAllByActivated() {
        return userRepository.findAllByActivated(true);
    }

    @Override
    public List<User> findAllByRole(UserRoles role) {
        List<User> allUsers = findAll();
        List<User> userList = new ArrayList<>();
        for (User user : allUsers) {
            if (user.getUserRoles().contains(role)) {
                userList.add(user);
            }
        }
        return userList;
    }

    @Override
    public List<User> findAllByRoles(String roles) {
        String[] roleNames = roles.split(",");
        List<UserRoles> rolesList = new ArrayList<>();
        for ( int i=0; i<roleNames.length; i++ ) {
            Optional<UserRoles> role = rolesRepository.findByName(roleNames[i]);
            role.ifPresent(rolesList::add);
        }

        List<User> allUsers = findAll();
        List<User> userList = new ArrayList<>();
        for (User u : allUsers) {
            for (UserRoles r : rolesList) {
                if (u.getUserRoles().contains(r) && !userList.contains(u)) {
                    userList.add(u);
                }
            }
        }
        return userList;
    }

    @Override
    public List<User> findAllByDeleted(boolean deleted) {
        return userRepository.findAllByDeleted(deleted);
    }

    @Override
    public UserDetails loadUserByUsername(String username) {
        Optional<User> user = userRepository.findByUsernameIgnoreCase(username);
        return user.orElse(null);
    }

    private User setNewUser(User user, boolean activated) {
        user.setActivated(activated);
        user.setDeleted(false);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setSubscriptions(new ArrayList<>());
        User newUser = userRepository.save(user);

        // profile Table
        profileService.createDefaultProfile(newUser);

        UserNotificationSubscribe subscription = subscribeRepository.save(
                new UserNotificationSubscribe(null, NotificationEndPoint.Email, newUser));
        user.getSubscriptions().add(subscription);
        userRepository.save(newUser);

        return newUser;
    }
}
