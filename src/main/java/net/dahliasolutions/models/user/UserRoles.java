package net.dahliasolutions.models.user;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigInteger;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class UserRoles {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "user_roles_gen")
    @SequenceGenerator(name = "user_roles_gen", sequenceName = "user_roles_seq", allocationSize = 1)
    private BigInteger id;
    private String name;
    private String description;
}
