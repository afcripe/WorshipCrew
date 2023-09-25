package net.dahliasolutions.models.support;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigInteger;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class TicketPriority {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @SequenceGenerator(name = "ticket_priority_gen", sequenceName = "ticket_priority_seq", allocationSize = 1)
    private BigInteger id;
    private int displayOrder;
    private String priority;

}
