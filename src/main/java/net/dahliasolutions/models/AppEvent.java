package net.dahliasolutions.models;

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
public class AppEvent {

    @Id
    private BigInteger id;
    private String name;
    private String Description;
    private String itemId;

    @Enumerated
    private EventModule module;

    @Enumerated
    private EventType type;

    @ManyToMany
    private List<User> users;

    @Override
    public String toString() {
        return "Notification{" +
                ", name='" + name + '\'' +
                ", Description='" + Description + '\'' +
                '}';
    }
}
