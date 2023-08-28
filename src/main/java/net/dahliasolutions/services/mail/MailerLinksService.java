package net.dahliasolutions.services.mail;

import lombok.RequiredArgsConstructor;
import net.dahliasolutions.data.MailerLinksRepository;
import net.dahliasolutions.models.mail.MailerLinks;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MailerLinksService implements MailerLinksServiceInterface {

    private final MailerLinksRepository mailerLinksRepository;

    @Override
    public MailerLinks createLink(MailerLinks mailerLinks) {
        return mailerLinksRepository.save(mailerLinks);
    }

    @Override
    public void save(MailerLinks mailerLinks) {
        mailerLinksRepository.save(mailerLinks);
    }

    @Override
    public Optional<MailerLinks> findByRandomLinkString(String randomString) {
        return mailerLinksRepository.findByRandomLinkString(randomString);
    }

    @Override
    public List<MailerLinks> findAll() {
        return mailerLinksRepository.findAll();
    }

    @Override
    public List<MailerLinks> findByUserId(BigInteger id) {
        return mailerLinksRepository.findAllByUserId(id);
    }

    @Override
    public List<MailerLinks> findAllByTicketId(String id) {
        return mailerLinksRepository.findAllByTicketId(id);
    }

    @Override
    public List<MailerLinks> findNotExpired(LocalDateTime expiration) {
        return mailerLinksRepository.findNotExpired(LocalDateTime.now());
    }
}
