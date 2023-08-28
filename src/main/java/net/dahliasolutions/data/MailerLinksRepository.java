package net.dahliasolutions.data;

import net.dahliasolutions.models.mail.MailerLinks;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface MailerLinksRepository extends JpaRepository<MailerLinks, BigInteger> {

    Optional<MailerLinks> findByRandomLinkString(String randomString);
    List<MailerLinks> findAllByUserId(BigInteger id);
    List<MailerLinks> findAllByTicketId(String id);

    @Query(value="SELECT * FROM MAILER_LINKS WHERE EXPIRATION < :expiration AND FORCE_EXPIRE = FALSE", nativeQuery = true)
    List<MailerLinks> findNotExpired(@Param("expiration") LocalDateTime expiration);
}
