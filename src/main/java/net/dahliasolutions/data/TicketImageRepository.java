package net.dahliasolutions.data;

import net.dahliasolutions.models.support.TicketImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;

public interface TicketImageRepository extends JpaRepository<TicketImage, BigInteger> {

    Optional<TicketImage> findByName(String imageName);
    Optional<TicketImage> findByFileLocation(String fileLocation);

    @Query(value="SELECT * FROM TICKET_IMAGE WHERE UPPER(NAME) LIKE CONCAT('%',UPPER(:s),'%') OR UPPER(DESCRIPTION) LIKE CONCAT('%',UPPER(:s),'%')", nativeQuery = true)
    List<TicketImage> findBySearchTerm(@Param("s") String searchTerm);

}
