package net.dahliasolutions.services.user;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import net.dahliasolutions.data.*;
import net.dahliasolutions.models.campus.Campus;
import net.dahliasolutions.models.department.DepartmentCampus;
import net.dahliasolutions.models.position.Position;
import net.dahliasolutions.models.user.User;
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

    @Override
    public User createDefaultUser(User user) {
        user.setActivated(true);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        User newUser = userRepository.save(user);
        return newUser;
    }

    @Override
    public User createUser(User user) {
        user.setActivated(false);
        user.setPassword("-UserMustChangeThisByClickingOnTheEmailedLink-");
        User newUser = userRepository.save(user);
        return newUser;
    }

    @Override
    public boolean verifyByUsername(String username) {
        return userRepository.existsByUsername(username);
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
        Optional<User> user = userRepository.findByUsername(username);
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
    public User save(User user) {
        return userRepository.save(user);
    }

    @Override
    public List<User> findAll() {
        return userRepository.findAll();
    }

    @Override
    public List<User> findAllByCampus(Campus campus) {
        return userRepository.findAllByCampus(campus);
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Override
    public void updateUserPosition(String username, String positionName) {
        Optional<User> user = userRepository.findByUsername(username);
        Optional<Position> position = positionRepository.findByName(positionName);
        if (user.isPresent() && position.isPresent()) {
            user.get().setPosition(position.get());
            userRepository.save(user.get());
        }
    }

    @Override
    public void updateUserCampus(String username, String campusName) {
        Optional<User> user = userRepository.findByUsername(username);
        Optional<Campus> campus = campusRepository.findByName(campusName);
        if (user.isPresent() && campus.isPresent()) {
            user.get().setCampus(campus.get());
            userRepository.save(user.get());
        }
    }

    @Override
    public void updateUserDepartment(String username, String departmentName) {
        Optional<User> user = userRepository.findByUsername(username);
        Optional<DepartmentCampus> department = departmentCampusRepository.findByNameAndCampus(departmentName, user.get().getCampus());
        if (user.isPresent() && department.isPresent()) {
            user.get().setDepartment(department.get());
            userRepository.save(user.get());
        }
    }

    @Override
    public void updateUserLocationByLocationId(String username, BigInteger locationId) {
        Optional<User> user = userRepository.findByUsername(username);
        Optional<Campus> campus = campusRepository.findById(locationId);
        if (user.isPresent() && campus.isPresent()) {
            user.get().setCampus(campus.get());
            userRepository.save(user.get());
        }
    }

    @Override
    public List<User> findAllByPosition(Position position) {
        return userRepository.findAllByPosition(position);
    }

    @Override
    public List<User> findAllByDepartment(DepartmentCampus department) {
        return userRepository.findAllByDepartmentCampus(department);
    }

    @Override
    public List<User> findAllByLocation(Campus location) {
        return userRepository.findAllByCampus(location);
    }

    @Override
    public List<User> findAllByActivated() {
        return userRepository.findAllByActivated(true);
    }

    @Override
    public UserDetails loadUserByUsername(String username) {
        Optional<User> user = userRepository.findByUsername(username);

        return user.orElse(null);
    }
}
