package net.dahliasolutions.data;

import net.dahliasolutions.models.wiki.WikiImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;

public interface WikiImageRepository extends JpaRepository<WikiImage, BigInteger> {

    Optional<WikiImage> findByName(String imageName);
    Optional<WikiImage> findByFileLocation(String fileLocation);

    @Query(value="SELECT * FROM WIKI_IMAGE WHERE UPPER(NAME) LIKE CONCAT('%',UPPER(:s),'%') OR UPPER(DESCRIPTION) LIKE CONCAT('%',UPPER(:s),'%')", nativeQuery = true)
    List<WikiImage> findBySearchTerm(@Param("s") String searchTerm);

}
