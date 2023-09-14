package net.dahliasolutions.data;

import net.dahliasolutions.models.mail.UserBlackout;
import org.springframework.data.jpa.repository.JpaRepository;

import java.math.BigInteger;
import java.util.List;

public interface UserBlackoutRepository extends JpaRepository<UserBlackout, BigInteger> {

    List<UserBlackout> findAllByUserId(BigInteger userId);
    List<UserBlackout> findAllByActive(boolean active);
}
