package net.dahliasolutions.models.department;

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
public class DepartmentRegional {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "department_regional_gen")
    @SequenceGenerator(name = "department_regional_gen", sequenceName = "department_regional_seq", allocationSize = 1)
    private BigInteger id;
    private String name;
    private BigInteger directorId;
    private String directorName;

}
