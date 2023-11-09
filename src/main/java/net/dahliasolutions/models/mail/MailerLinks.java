package net.dahliasolutions.models.mail;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigInteger;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class MailerLinks {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "mailer_links_generator")
    @SequenceGenerator(name = "mailer_links_generator", sequenceName = "mailer_links_seq", allocationSize = 1)
    private BigInteger id;
    private BigInteger userId;
    private BigInteger serviceId;
    private String ticketId;
    private String randomLinkString;
    private LocalDateTime expiration;
    private boolean forceExpire;
    private String serviceMethod;
}
