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
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @SequenceGenerator(name = "notification_gen", sequenceName = "notification_seq", allocationSize = 1)
    private BigInteger id;
    private String name;
    private String Description;
    private String itemId;

    @Enumerated
    private EventModule module;

    @Enumerated
    private EventType type;

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
