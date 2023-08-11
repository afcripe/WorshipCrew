package net.dahliasolutions.models.order;

import lombok.*;
import net.dahliasolutions.models.campus.Campus;

import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderRequestCampus {

    private Campus campus;
    private List<OrderRequest> requestList;
}
