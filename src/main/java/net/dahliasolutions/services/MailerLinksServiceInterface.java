package net.dahliasolutions.services;

import net.dahliasolutions.models.MailerLinks;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface MailerLinksServiceInterface {

    MailerLinks createLink(MailerLinks mailerLinks);
    void save(MailerLinks mailerLinks);
    Optional<MailerLinks> findByRandomLinkString(String randomString);
    List<MailerLinks> findAll();
    List<MailerLinks> findByUserId(BigInteger id);
    List<MailerLinks> findNotExpired(LocalDateTime expiration);
}
