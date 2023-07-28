package net.dahliasolutions.models.store;

import jakarta.annotation.Nullable;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.dahliasolutions.models.department.DepartmentRegional;
import net.dahliasolutions.models.position.Position;

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
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private BigInteger id;
    private String name;

    @OneToMany(fetch = FetchType.EAGER, mappedBy = "storeCategory")
    private List<StoreSubCategory> subCategoryList;

}
