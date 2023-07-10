package net.dahliasolutions.data;

import net.dahliasolutions.models.Position;
import org.springframework.data.jpa.repository.JpaRepository;

import java.math.BigInteger;
import java.util.Optional;

public interface PositionRepository extends JpaRepository<Position, BigInteger> {

    Optional<Position> findByName(String positionName);
}
