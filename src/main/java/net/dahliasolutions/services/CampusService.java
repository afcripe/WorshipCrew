package net.dahliasolutions.services;

import lombok.RequiredArgsConstructor;
import net.dahliasolutions.data.CampusRepository;
import net.dahliasolutions.models.Campus;
import net.dahliasolutions.models.User;
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
    public Campus createCampus(String name, String city, BigInteger managerId) {
        Campus location = new Campus(null, name, city, false, managerId, "");
        Optional<User> mgr = userService.findById(managerId);
        mgr.ifPresent(user -> location.setManagerName(user.getFirstName() + " " + user.getLastName()));
        return campusRepository.save(location);
    }

    @Override
    public Campus createCampus(Campus location) {
        return campusRepository.save(location);
    }

    @Override
    public Optional<Campus> findById(BigInteger id) {
        return updateCampusManagerName(campusRepository.findById(id));
    }

    @Override
    public Optional<Campus> findByName(String name) {
        return updateCampusManagerName(campusRepository.findByName(name));
    }

    @Override
    public Optional<Campus> findByCity(String city) {
        return updateCampusManagerName(campusRepository.findByCity(city));
    }

    @Override
    public List<Campus> findAll() {
        List<Campus> locationList = campusRepository.findAllByHiddenNot();
        for(Campus loc : locationList) {
            updateCampusManagerName(campusRepository.findById(loc.getId()));
        }
        return locationList;
    }

    @Override
    public List<Campus> findAllIncludeHidden() {
        List<Campus> locationList = campusRepository.findAll();
        for(Campus loc : locationList) {
            updateCampusManagerName(campusRepository.findById(loc.getId()));
        }
        return locationList;
    }

    @Override
    public List<Campus> findAllByManager(BigInteger id) {
        List<Campus> locationList = campusRepository.findAllByManager(id);
        for(Campus loc : locationList) {
            updateCampusManagerName(campusRepository.findById(loc.getId()));
        }
        return locationList;
    }

    @Override
    public void save(Campus location) {
        location = updateCampusManagerName(campusRepository.findById(location.getId())).orElse(null);
        if (location != null) {campusRepository.save(location);}
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
    public Optional<Campus> updateCampusManagerName(Optional<Campus> location) {
        if (location.isPresent()) {
            Optional<User> mgr = userService.findById(location.get().getManagerId());
            if (mgr.isPresent()) {
                location.get().setManagerName(mgr.get().getFirstName()+" "+mgr.get().getLastName());
            } else {
                location.get().setManagerName("");
            }
            return location;
        }
        return location;
    }
}
