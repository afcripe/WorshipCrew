package net.dahliasolutions.services.position;

import net.dahliasolutions.models.position.Position;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;

public interface PositionServiceInterface {

    Optional<Position> findById(BigInteger id);
    Optional<Position> findByName(String positionName);
    List<Position> findAll();
    List<Position> findAllByLevelGreaterThan(int level);
    Optional<Position> findFirst1OrderByLevelDesc();
    Position createPosition(String name);
    Position save(Position position);
    void updatePosition(Position position);
    void deletePositionById(BigInteger id);
    List<Position> findAllByDirectorId(BigInteger id);
    Optional<Position> updatePositionDirectorName(Optional<Position> location);
}
