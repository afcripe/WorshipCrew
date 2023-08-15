package net.dahliasolutions.models;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigInteger;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Event {

    private String name;
    private String Description;
    private BigInteger itemId;

    @Enumerated
    private EventModule module;

    @Enumerated
    private EventType type;

    @Override
    public String toString() {
        return "Notification{" +
                ", name='" + name + '\'' +
                ", Description='" + Description + '\'' +
                '}';
    }
}
