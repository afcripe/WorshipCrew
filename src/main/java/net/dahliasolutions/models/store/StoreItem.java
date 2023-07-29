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
public class StoreItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private BigInteger id;
    private String name;
    private boolean specialOrder;
    private boolean available;
    private int leadTime;

    @Lob
    @Column(name = "description", columnDefinition="BLOB")
    private String description;

    @Nullable
    @ManyToOne(fetch = FetchType.LAZY)
    private StoreImage image;

    @ManyToOne
    private StoreCategory category;

    @ManyToOne
    private StoreSubCategory subCategory;

    @Nullable
    @ManyToOne(fetch = FetchType.EAGER)
    private DepartmentRegional department;

    @ManyToMany(fetch = FetchType.EAGER)
    private Collection<Position> positionList;

    @OneToMany(fetch = FetchType.EAGER)
    private List<StoreItemOption> itemOptions;

}
