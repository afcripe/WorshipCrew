package net.dahliasolutions.models.mail;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.dahliasolutions.models.user.User;

import java.math.BigInteger;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class MailerCustomMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @SequenceGenerator(name = "mailer_custom_message_generator", sequenceName = "mailer_custom_message_seq", allocationSize = 1)
    private BigInteger id;
    private String subject;
    private boolean draft;

    @Column(name = "messageBody", columnDefinition = "text")
    private String messageBody;

    @OneToOne(fetch = FetchType.EAGER)
    private User user;

    @OneToMany
    private List<User> toUsers;
}
