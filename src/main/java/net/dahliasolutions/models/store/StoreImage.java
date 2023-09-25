package net.dahliasolutions.models.store;

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
public class StoreImage {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @SequenceGenerator(name = "store_image_gen", sequenceName = "store_image_seq", allocationSize = 1)
    private BigInteger id;
    private String name;
    private String description;
    private String fileLocation;
}
