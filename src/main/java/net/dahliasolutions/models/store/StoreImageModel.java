package net.dahliasolutions.models.store;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigInteger;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StoreImageModel {

    @Id
    private BigInteger id;
    private String name;
    private String description;
    private String fileLocation;
    private int references;
}
