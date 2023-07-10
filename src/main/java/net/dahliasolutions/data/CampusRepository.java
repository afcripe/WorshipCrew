package net.dahliasolutions.data;

import net.dahliasolutions.models.Campus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;

public interface CampusRepository extends JpaRepository<Campus, BigInteger> {

    Optional<Campus> findByName(String name);
    Optional<Campus> findByCity(String city);

    @Query(value="SELECT * FROM LOCATION WHERE MANAGER_ID = :id", nativeQuery = true)
    List<Campus> findAllByManager(@Param("id") BigInteger id);

    @Query(value="SELECT * FROM LOCATION WHERE HIDDEN = false", nativeQuery = true)
    List<Campus> findAllByHiddenNot();
}
