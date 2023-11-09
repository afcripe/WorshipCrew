package net.dahliasolutions.models.position;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigInteger;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Position {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "position_gen")
    @SequenceGenerator(name = "position_gen", sequenceName = "position_seq", allocationSize = 1)
    private BigInteger id;
    private int level;
    private String name;
    private BigInteger directorId;
    private String directorName;
}
