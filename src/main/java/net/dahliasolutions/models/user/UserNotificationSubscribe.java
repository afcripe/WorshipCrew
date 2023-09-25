package net.dahliasolutions.models.user;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.dahliasolutions.models.NotificationEndPoint;

import java.math.BigInteger;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class UserNotificationSubscribe {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @SequenceGenerator(name = "user_notification_subscribe_gen", sequenceName = "user_notification_subscribe_seq", allocationSize = 1)
    private BigInteger id;

    @Enumerated
    private NotificationEndPoint endPoint;

    @ManyToOne
    @JsonIgnore
    private User user;
}
