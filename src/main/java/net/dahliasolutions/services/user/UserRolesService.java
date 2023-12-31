package net.dahliasolutions.services.user;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import net.dahliasolutions.data.UserRolesRepository;
import net.dahliasolutions.models.user.UserRoles;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;


@Service
@Transactional
@RequiredArgsConstructor
public class UserRolesService implements UserRolesServiceInterface{

    private final UserRolesRepository rolesRepository;

    @Override
    public void createRole(String name, String description) {
        UserRoles role = new UserRoles(null, name, description);
        rolesRepository.save(role);
    }

    @Override
    public void save(UserRoles role) {
        rolesRepository.save(role);
    }

    @Override
    public Optional<UserRoles> findByName(String name) {
        return rolesRepository.findByName(name);
    }

    @Override
    public List<UserRoles> findAll() {
        return rolesRepository.findAll();
    }

    @Override
    public Optional<UserRoles> findById(BigInteger id) {
        return rolesRepository.findById(id);
    }

}
