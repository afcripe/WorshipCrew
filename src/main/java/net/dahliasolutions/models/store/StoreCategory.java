package net.dahliasolutions.models.store;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigInteger;
import java.util.Collection;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class StoreCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "store_category_gen")
    @SequenceGenerator(name = "store_category_gen", sequenceName = "store_category_seq", allocationSize = 1)
    private BigInteger id;
    private String name;

    @OneToMany(fetch = FetchType.EAGER)
    private List<StoreSubCategory> subCategoryList;

}
