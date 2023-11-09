package net.dahliasolutions.models.store;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
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
public class StoreItemOption {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "store_item_option_gen")
    @SequenceGenerator(name = "store_item_option_gen", sequenceName = "store_item_option_gen", allocationSize = 1)
    private BigInteger id;
    private String name;

    @JsonManagedReference
    @ManyToOne
    @JoinColumn(name = "storeItemId")
    private StoreItem storeItem;

}
