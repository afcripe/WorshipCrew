package net.dahliasolutions.models.position;


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
public class PositionSelectedModel {

    @Id
    private BigInteger id;
    private int level;
    private String name;
    private boolean selected;
}
