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
    @Query(value = "SELECT * FROM POSITION WHERE LEVEL >= :level", nativeQuery = true)
    List<Position> findAllByLevelGreaterThanOrLevelEquals(@Param("level") int level);
    @Query(value = "SELECT * FROM POSITION ORDER BY LEVEL DESC LIMIT 1", nativeQuery = true)
    Optional<Position> findFirst1OrderByLevelDesc();

    @Query(value="SELECT * FROM POSITION WHERE DIRECTOR_ID = :id", nativeQuery = true)
    List<Position> findAllByDirectorId(@Param("id") BigInteger id);

}
