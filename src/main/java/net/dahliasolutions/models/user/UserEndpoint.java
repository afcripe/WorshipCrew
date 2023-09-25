package net.dahliasolutions.models.user;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
    @GeneratedValue(strategy = GenerationType.AUTO)
    @SequenceGenerator(name = "user_endpoint_gen", sequenceName = "user_endpoint_seq", allocationSize = 1)
    private BigInteger id;
    private String name;
    private String token;

    @ManyToOne
    @JsonIgnore
    private User user;
}
