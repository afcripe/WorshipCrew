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

    private BigInteger id;
    private String name;
    private String Description;
    private BigInteger itemId;
    private String ticketId;

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
