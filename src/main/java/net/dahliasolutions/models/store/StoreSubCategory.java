package net.dahliasolutions.models.store;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
public class StoreSubCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @SequenceGenerator(name = "store_sub_category_gen", sequenceName = "store_sub_category_gen", allocationSize = 1)
    private BigInteger id;
    private String name;
    private BigInteger categoryId;

}
