package net.dahliasolutions.models.support;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigInteger;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class SLA {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "sla_gen")
    @SequenceGenerator(name = "sla_gen", sequenceName = "sla_seq", allocationSize = 1)
    private BigInteger id;
    private String name;
    private String description;
    private int  completionDue;       // hours from ticket submission to resolve
}
