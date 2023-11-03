package net.dahliasolutions.models;

import jakarta.persistence.*;
import lombok.*;
import net.dahliasolutions.models.user.User;

import java.math.BigInteger;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class NotificationMessageModel {

    private BigInteger id;
    private String subject;
    private LocalDateTime dateSent;
    private boolean read;
    private String fromUser;
    private String module;
    private String messageBody;

    @Override
    public String toString() {
        return "Notification{" +
                "id=" + id +
                ", name='" + subject + '\'' +
                '}';
    }
}
