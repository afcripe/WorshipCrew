package net.dahliasolutions.models;

import jakarta.annotation.Nullable;
import jakarta.persistence.*;
import lombok.*;
import net.dahliasolutions.models.user.User;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class NotificationMessage {

    @Id
    @SequenceGenerator(name = "notification_msg_gen", sequenceName = "notification_msg_gen", allocationSize = 1)
    @GeneratedValue(generator = "notification_msg_gen", strategy = GenerationType.SEQUENCE)
    private BigInteger id;
    private String subject;
    private String moduleId;
    private BigInteger itemId;
    private BigInteger eventId;
    private boolean sendNow;
    private boolean sent;
    private LocalDateTime dateSent;

    @Enumerated
    private EventModule module;

    @Enumerated
    private EventType type;

    @ManyToOne
    private User user;

    private BigInteger noteId;

    @Override
    public String toString() {
        return "Notification{" +
                "id=" + id +
                ", name='" + subject + '\'' +
                ", module='" + moduleId + '\'' +
                '}';
    }
}
