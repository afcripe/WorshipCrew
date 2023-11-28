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
    @GeneratedValue(generator = "notification_msg_gen", strategy = GenerationType.SEQUENCE)
    @SequenceGenerator(name = "notification_msg_gen", sequenceName = "notification_msg_seq", allocationSize = 1)
    private BigInteger id;
    private String subject;
    private String moduleId;
    private BigInteger itemId;
    private BigInteger eventId;
    private boolean sendNow;
    private boolean sent;
    private LocalDateTime dateSent;

    @Column(columnDefinition = "boolean default true")
    private boolean read;

    @Column(columnDefinition = "numeric default 0")
    private BigInteger fromUserId;

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
