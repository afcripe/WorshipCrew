package net.dahliasolutions.models.user;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigInteger;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class UserEndpoint {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private BigInteger id;
    private String name;
    private String token;

    @ManyToOne
    private User user;
}
