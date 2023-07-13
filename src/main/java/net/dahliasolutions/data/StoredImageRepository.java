package net.dahliasolutions.data;

import net.dahliasolutions.models.StoredImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;

public interface StoredImageRepository extends JpaRepository<StoredImage, BigInteger> {

    Optional<StoredImage> findByName(String imageName);
    Optional<StoredImage> findByFileLocation(String fileLocation);
    @Query(value="SELECT * FROM STORED_IMAGE WHERE UPPER(NAME) LIKE CONCAT('%',UPPER(:s),'%') OR UPPER(DESCRIPTION) LIKE CONCAT('%',UPPER(:s),'%')", nativeQuery = true)
    List<StoredImage> findBySearchTerm(@Param("s") String searchTerm);

}
