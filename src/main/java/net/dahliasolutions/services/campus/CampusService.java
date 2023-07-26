package net.dahliasolutions.services.campus;

import lombok.RequiredArgsConstructor;
import net.dahliasolutions.data.CampusRepository;
import net.dahliasolutions.models.campus.Campus;
import net.dahliasolutions.models.user.User;
import net.dahliasolutions.services.user.UserService;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CampusService implements CampusServiceInterface {

    private final CampusRepository campusRepository;
    private final UserService userService;

    @Override
    public Campus createCampus(String name, String city, BigInteger DirectorId) {
        Campus location = new Campus(null, name, city, false, DirectorId, "");
        Optional<User> mgr = userService.findById(DirectorId);
        mgr.ifPresent(user -> location.setDirectorName(user.getFirstName() + " " + user.getLastName()));
        return campusRepository.save(location);
    }

    @Override
    public Campus createCampus(Campus location) {
        return campusRepository.save(location);
    }

    @Override
    public Optional<Campus> findById(BigInteger id) {
        return updateCampusDirectorName(campusRepository.findById(id));
    }

    @Override
    public Optional<Campus> findByName(String name) {
        return updateCampusDirectorName(campusRepository.findByName(name));
    }

    @Override
    public Optional<Campus> findByCity(String city) {
        return updateCampusDirectorName(campusRepository.findByCity(city));
    }

    @Override
    public List<Campus> findAll() {
        List<Campus> locationList = campusRepository.findAllByHiddenNot();
        for(Campus loc : locationList) {
            updateCampusDirectorName(campusRepository.findById(loc.getId()));
        }
        return locationList;
    }

    @Override
    public List<Campus> findAllIncludeHidden() {
        List<Campus> locationList = campusRepository.findAll();
        for(Campus loc : locationList) {
            updateCampusDirectorName(campusRepository.findById(loc.getId()));
        }
        return locationList;
    }

    @Override
    public List<Campus> findAllByDirector(BigInteger id) {
        List<Campus> locationList = campusRepository.findAllByDirector(id);
        for(Campus loc : locationList) {
            updateCampusDirectorName(campusRepository.findById(loc.getId()));
        }
        return locationList;
    }

    @Override
    public void save(Campus location) {
        campusRepository.save(updateCampusDirectorName(Optional.ofNullable(location)).get());
    }

    @Override
    public void deleteById(BigInteger id) {
        Optional<Campus> location = findById(id);
        if (location.isPresent()) {
            location.get().setHidden(true);
            campusRepository.save(location.get());
        }
    }

    @Override
    public void restoreById(BigInteger id) {
        Optional<Campus> location = findById(id);
        if (location.isPresent()) {
            location.get().setHidden(false);
            campusRepository.save(location.get());
        }
    }

    @Override
    public Optional<Campus> updateCampusDirectorName(Optional<Campus> location) {
        if (location.isPresent() && location.get().getDirectorId() != null) {
            Optional<User> mgr = userService.findById(location.get().getDirectorId());
            if (mgr.isPresent()) {
                location.get().setDirectorName(mgr.get().getFirstName()+" "+mgr.get().getLastName());
            } else {
                location.get().setDirectorName("");
            }
            return location;
        }
        return location;
    }
}
