package net.dahliasolutions.data;

import net.dahliasolutions.models.mail.CorporateBlackout;
import org.springframework.data.jpa.repository.JpaRepository;

import java.math.BigInteger;
import java.util.List;

public interface CorporateBlackoutRepository extends JpaRepository<CorporateBlackout, BigInteger> {

    List<CorporateBlackout> findAllByUserId(BigInteger userId);
    List<CorporateBlackout> findAllByActive(boolean active);
}
