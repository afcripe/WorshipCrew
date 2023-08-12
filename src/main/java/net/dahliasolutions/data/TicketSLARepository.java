package net.dahliasolutions.data;

import net.dahliasolutions.models.support.SLA;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigInteger;
import java.util.Optional;

public interface TicketSLARepository extends JpaRepository<SLA, BigInteger> {

    @Query(value="SELECT * FROM SLA ORDER BY RESPONSE_LEVEL DESC", nativeQuery = true)
    Optional<SLA> findFirstOrderByResponseLevelDesc();

}
