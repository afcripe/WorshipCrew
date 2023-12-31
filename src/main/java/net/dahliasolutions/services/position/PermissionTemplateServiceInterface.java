package net.dahliasolutions.services.position;

import net.dahliasolutions.models.position.PermissionTemplate;
import net.dahliasolutions.models.position.Position;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;

public interface PermissionTemplateServiceInterface {

    Optional<PermissionTemplate> findById(BigInteger id);
    Optional<PermissionTemplate> findByName(String name);
    List<PermissionTemplate> findAllByPosition(Position position);
    Optional<PermissionTemplate> findFirstByPosition(Position position);
    Optional<PermissionTemplate> findDefaultByPosition(Position position);
    List<PermissionTemplate> findAll();
    void save(PermissionTemplate permissionTemplate);
    void deletePermissionTemplateById(BigInteger id);
}
