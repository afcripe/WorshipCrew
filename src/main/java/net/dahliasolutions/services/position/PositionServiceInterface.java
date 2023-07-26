package net.dahliasolutions.services.position;

import net.dahliasolutions.models.position.Position;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;

public interface PositionServiceInterface {

    Optional<Position> findById(BigInteger id);
    Optional<Position> findByName(String positionName);
    List<Position> findAll();
    Position createPosition(String name);
    void updatePosition(Position position);
    void deletePositionById(BigInteger id);
    List<Position> findAllByDirectorId(BigInteger id);
    Optional<Position> updatePositionDirectorName(Optional<Position> location);
}
