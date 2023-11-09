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
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "mailer_custom_message_gen")
    @SequenceGenerator(name = "mailer_custom_message_gen", sequenceName = "mailer_custom_message_seq", allocationSize = 1)
    private BigInteger id;
    private String subject;
    private boolean draft;

    @Column(name = "messageBody", columnDefinition = "text")
    private String messageBody;

    @ManyToOne(fetch = FetchType.EAGER)
    private User user;

    @ManyToMany(fetch = FetchType.EAGER)
    private List<User> toUsers;
}
