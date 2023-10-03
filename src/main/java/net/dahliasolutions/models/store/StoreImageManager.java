package net.dahliasolutions.models.store;

import lombok.*;

import java.math.BigInteger;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class StoreImageManager {

    private BigInteger id;
    private StoreImageModel image;
    private List<StoreItem> items;
}
