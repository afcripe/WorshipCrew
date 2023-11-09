package net.dahliasolutions.models.position;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.dahliasolutions.models.user.UserRoles;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class PermissionTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "position_template_gen")
    @SequenceGenerator(name = "position_template_gen", sequenceName = "position_template_seq", allocationSize = 1)
    private BigInteger id;
    private String name;
    private boolean defaultTemplate;

    @ManyToOne
    private Position position;

    @ManyToMany(fetch = FetchType.EAGER)
    private List<UserRoles> userRoles = new ArrayList<>();

}
