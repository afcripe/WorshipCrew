package net.dahliasolutions.data;

import net.dahliasolutions.models.position.PermissionTemplate;
import net.dahliasolutions.models.position.Position;
import org.springframework.data.jpa.repository.JpaRepository;

import java.math.BigInteger;
import java.util.Optional;

public interface PermissinoTemplateRepository extends JpaRepository<PermissionTemplate, BigInteger> {

    Optional<PermissionTemplate> findByName(String name);
    Optional<PermissionTemplate> findFirstByPosition(Position position);
}
