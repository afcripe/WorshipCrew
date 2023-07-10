package net.dahliasolutions.services;

import lombok.RequiredArgsConstructor;
import net.dahliasolutions.data.PositionRepository;
import net.dahliasolutions.models.Position;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PositionService implements PositionServiceInterface {

    private final PositionRepository positionRepository;

    @Override
    public Optional<Position> findById(BigInteger id) {
        return positionRepository.findById(id);
    }

    @Override
    public Optional<Position> findByName(String positionName) {
        return positionRepository.findByName(positionName);
    }

    @Override
    public List<Position> findAll() {
        return positionRepository.findAll();
    }

    @Override
    public Position createPosition(String name) {
        Position position = new Position(null, name);
        return positionRepository.save(position);
    }

    @Override
    public void updatePosition(Position position) {
        positionRepository.save(position);
    }

    @Override
    public void deletePositionById(BigInteger id) {
        positionRepository.deleteById(id);
    }
}
