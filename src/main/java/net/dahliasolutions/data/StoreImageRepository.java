package net.dahliasolutions.data;

import net.dahliasolutions.models.store.StoreImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;

public interface StoreImageRepository extends JpaRepository<StoreImage, BigInteger> {

    Optional<StoreImage> findByName(String imageName);
    Optional<StoreImage> findByFileLocation(String fileLocation);
    @Query(value="SELECT * FROM STORE_IMAGE WHERE UPPER(NAME) LIKE CONCAT('%',UPPER(:s),'%') OR UPPER(DESCRIPTION) LIKE CONCAT('%',UPPER(:s),'%')", nativeQuery = true)
    List<StoreImage> findBySearchTerm(@Param("s") String searchTerm);

}
