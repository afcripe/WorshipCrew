package net.dahliasolutions.services.position;

import lombok.RequiredArgsConstructor;
import net.dahliasolutions.data.PermissinoTemplateRepository;
import net.dahliasolutions.models.position.PermissionTemplate;
import net.dahliasolutions.models.position.Position;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PermissionTemplateService implements PermissionTemplateServiceInterface {

    private final PermissinoTemplateRepository permissinoTemplateRepository;

    @Override
    public Optional<PermissionTemplate> findById(BigInteger id) {
        return permissinoTemplateRepository.findById(id);
    }

    @Override
    public Optional<PermissionTemplate> findByName(String name) {
        return permissinoTemplateRepository.findByName(name);
    }

    @Override
    public Optional<PermissionTemplate> findFirstByPosition(Position position) {
        return permissinoTemplateRepository.findFirstByPosition(position);
    }

    @Override
    public List<PermissionTemplate> findAll() {
        return permissinoTemplateRepository.findAll();
    }

    @Override
    public void save(PermissionTemplate permissionTemplate) {
        permissinoTemplateRepository.save(permissionTemplate);
    }

    @Override
    public void deletePermissionTemplateById(BigInteger id) {
        permissinoTemplateRepository.deleteById(id);
    }
}
