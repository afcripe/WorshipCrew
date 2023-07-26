package net.dahliasolutions.data;

import net.dahliasolutions.models.position.Position;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;

public interface PositionRepository extends JpaRepository<Position, BigInteger> {

    Optional<Position> findByName(String positionName);

    @Query(value="SELECT * FROM POSITION WHERE DIRECTOR_ID = :id", nativeQuery = true)
    List<Position> findAllByDirectorId(@Param("id") BigInteger id);
}
