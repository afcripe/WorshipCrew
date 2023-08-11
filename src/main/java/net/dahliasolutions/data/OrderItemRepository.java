package net.dahliasolutions.data;

import net.dahliasolutions.models.department.DepartmentRegional;
import net.dahliasolutions.models.order.OrderItem;
import net.dahliasolutions.models.order.OrderRequest;
import net.dahliasolutions.models.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.math.BigInteger;
import java.util.List;

public interface OrderItemRepository extends JpaRepository<OrderItem, BigInteger> {

    List<OrderItem> findAllByOrderRequest(OrderRequest orderRequest);
    List<OrderItem> findAllBySupervisor(User user);
    List<OrderItem> findAllByDepartment(DepartmentRegional department);

}
