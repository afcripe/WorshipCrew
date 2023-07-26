package net.dahliasolutions.services.position;

import lombok.RequiredArgsConstructor;
import net.dahliasolutions.data.PositionRepository;
import net.dahliasolutions.models.position.Position;
import net.dahliasolutions.models.user.User;
import net.dahliasolutions.services.user.UserService;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PositionService implements PositionServiceInterface {

    private final PositionRepository positionRepository;
    private final UserService userService;

    @Override
    public Optional<Position> findById(BigInteger id) {
        return updatePositionDirectorName(positionRepository.findById(id));
    }

    @Override
    public Optional<Position> findByName(String positionName) {
        return updatePositionDirectorName(positionRepository.findByName(positionName));
    }

    @Override
    public List<Position> findAll() {
        List<Position> positionList = positionRepository.findAll();
        for(Position p : positionList) {
            updatePositionDirectorName(positionRepository.findById(p.getId()));
        }
        return positionList;
    }

    @Override
    public Position createPosition(String name) {
        Position position = new Position(null, name, null, "");
        return positionRepository.save(position);
    }

    @Override
    public void updatePosition(Position position) {
        positionRepository.save(updatePositionDirectorName(Optional.ofNullable(position)).get());
    }

    @Override
    public void deletePositionById(BigInteger id) {
        positionRepository.deleteById(id);
    }

    @Override
    public List<Position> findAllByDirectorId(BigInteger id) {
        List<Position> positionList = positionRepository.findAllByDirectorId(id);
        for(Position p : positionList) {
            updatePositionDirectorName(positionRepository.findById(p.getId()));
        }
        return positionList;
    }

    @Override
    public Optional<Position> updatePositionDirectorName(Optional<Position> position) {
        if (position.isPresent() && position.get().getDirectorId() != null) {
            Optional<User> mgr = userService.findById(position.get().getDirectorId());
            if (mgr.isPresent()) {
                position.get().setDirectorName(mgr.get().getFirstName()+" "+mgr.get().getLastName());
            } else {
                position.get().setDirectorName("");
            }
            return position;
        }
        return position;
    }
}
