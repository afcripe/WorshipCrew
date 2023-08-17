package net.dahliasolutions.models;

import jakarta.annotation.Nullable;
import jakarta.persistence.*;
import lombok.*;
import net.dahliasolutions.models.user.User;

import java.math.BigInteger;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Message {

    @Id
    private BigInteger id;
    private String name;
    private String Description;

    @Enumerated
    private EventModule module;

    @Enumerated
    private EventType type;

    @ManyToOne(fetch = FetchType.LAZY)
    private Notification notification;

    @Nullable
    @ManyToMany
    private List<User> users;

    @Override
    public String toString() {
        return "Notification{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", Description='" + Description + '\'' +
                '}';
    }
}
