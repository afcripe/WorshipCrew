package net.dahliasolutions.models;

import jakarta.persistence.Enumerated;
import lombok.*;
import net.dahliasolutions.models.user.User;

import java.math.BigInteger;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationEvent {

    private BigInteger id;
    private String subject;
    private String moduleId;
    private BigInteger itemId;

    @Enumerated
    private EventModule module;

    @Enumerated
    private NotificationType type;

    private User user;
}
