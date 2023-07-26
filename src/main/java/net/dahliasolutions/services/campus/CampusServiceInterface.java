package net.dahliasolutions.services.campus;

import net.dahliasolutions.models.campus.Campus;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;

public interface CampusServiceInterface {

    Campus createCampus(String name, String city, BigInteger managerId);
    Campus createCampus(Campus location);
    Optional<Campus> findById(BigInteger id);
    Optional<Campus> findByName(String name);
    Optional<Campus> findByCity(String city);
    List<Campus> findAll();
    List<Campus> findAllIncludeHidden();
    List<Campus> findAllByDirector(BigInteger id);
    void save(Campus location);
    void deleteById(BigInteger id);
    void restoreById(BigInteger id);
    Optional<Campus> updateCampusDirectorName(Optional<Campus> location);

}
