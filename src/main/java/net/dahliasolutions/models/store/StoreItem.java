package net.dahliasolutions.models.store;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.annotation.Nullable;
import jakarta.persistence.*;
import lombok.*;
import net.dahliasolutions.models.department.DepartmentRegional;
import net.dahliasolutions.models.position.Position;

import java.math.BigInteger;
import java.util.Collection;
import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class StoreItem {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @SequenceGenerator(name = "store_item_gen", sequenceName = "store_item_seq", allocationSize = 1)
    private BigInteger id;
    private String name;
    private boolean specialOrder;
    private boolean available;
    private int leadTime;

    @Column(name = "description", columnDefinition = "text")
    private String description;

    @Nullable
    @ManyToOne(fetch = FetchType.LAZY)
    private StoreImage image;

    @ManyToOne
    private StoreCategory category;

    @ManyToOne
    private StoreSubCategory subCategory;

    @ManyToOne(fetch = FetchType.EAGER)
    private DepartmentRegional department;

    @ManyToMany(fetch = FetchType.EAGER)
    private Collection<Position> positionList;

    @JsonBackReference
    @OneToMany(fetch = FetchType.EAGER, mappedBy = "storeItem")
    private List<StoreItemOption> itemOptions;

}
