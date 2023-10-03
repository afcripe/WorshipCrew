package net.dahliasolutions.data;

import net.dahliasolutions.models.department.DepartmentRegional;
import net.dahliasolutions.models.order.OrderItem;
import net.dahliasolutions.models.order.OrderRequest;
import net.dahliasolutions.models.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.List;

public interface OrderItemRepository extends JpaRepository<OrderItem, BigInteger> {

    List<OrderItem> findAllByOrderRequest(OrderRequest orderRequest);
    List<OrderItem> findAllBySupervisor(User user);
    List<OrderItem> findAllByDepartment(DepartmentRegional department);
    List<OrderItem> findAllByProductId(BigInteger productId);
    @Query(value = "SELECT * FROM ORDER_ITEM WHERE SUPERVISOR_ID = :userId AND REQUEST_DATE BETWEEN :start AND :end ORDER BY REQUEST_DATE DESC", nativeQuery = true)
    List<OrderItem> findAllBySupervisorAndCycle(@Param("userId") BigInteger userId, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
    @Query(value = "SELECT * FROM ORDER_ITEM WHERE DEPARTMENT_ID = :departmentId AND REQUEST_DATE BETWEEN :start AND :end ORDER BY REQUEST_DATE DESC", nativeQuery = true)
    List<OrderItem> findAllByDepartmentAndCycle(@Param("departmentId") BigInteger departmentId, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query(value = "SELECT * FROM ORDER_ITEM WHERE REQUEST_DATE BETWEEN :start AND :end ORDER BY REQUEST_DATE DESC", nativeQuery = true)
    List<OrderItem> findAllOrderByCycle(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);


}
