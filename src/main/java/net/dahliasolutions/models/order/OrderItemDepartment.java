package net.dahliasolutions.models.order;

import lombok.*;
import net.dahliasolutions.models.department.DepartmentRegional;

import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderItemDepartment {

    private DepartmentRegional department;
    private List<OrderItem> orderItemList;

}
