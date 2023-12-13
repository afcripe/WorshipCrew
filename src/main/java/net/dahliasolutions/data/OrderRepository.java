package net.dahliasolutions.data;

import jakarta.persistence.Tuple;
import net.dahliasolutions.models.campus.Campus;
import net.dahliasolutions.models.order.OrderRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<OrderRequest, BigInteger> {

    List<OrderRequest> findAllByCampus(Campus campus);
    List<OrderRequest> findAllByCampusOrderByRequestDateDesc(Campus campus);
    @Query(value = "SELECT * FROM ORDER_REQUEST WHERE ORDER_STATUS <> 'Cancelled' AND ORDER_STATUS <> 'Complete' ORDER BY REQUEST_DATE DESC", nativeQuery = true)
    List<OrderRequest> findAllOpenOnly();
    @Query(value = "SELECT * FROM ORDER_REQUEST WHERE ID = :id AND REQUEST_DATE BETWEEN :start AND :end ORDER BY REQUEST_DATE DESC", nativeQuery = true)
    Optional<OrderRequest> findAllByIdAndCycle(@Param("id") BigInteger id, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
    @Query(value = "SELECT * FROM ORDER_REQUEST WHERE USER_ID = :userId AND REQUEST_DATE BETWEEN :start AND :end ORDER BY REQUEST_DATE DESC", nativeQuery = true)
    List<OrderRequest> findAllByUserAndCycle(@Param("userId") BigInteger userId, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
    @Query(value = "SELECT * FROM ORDER_REQUEST WHERE CAMPUS_ID = :campusId AND REQUEST_DATE BETWEEN :start AND :end ORDER BY REQUEST_DATE DESC", nativeQuery = true)
    List<OrderRequest> findAllByCampusAndCycle(@Param("campusId") BigInteger campusId, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
    @Query(value = "SELECT * FROM ORDER_REQUEST WHERE SUPERVISOR_ID = :supervisorId AND REQUEST_DATE BETWEEN :start AND :end ORDER BY REQUEST_DATE DESC", nativeQuery = true)
    List<OrderRequest> findAllBySupervisorAndCycle(@Param("supervisorId") BigInteger supervisorId, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
    @Query(value = "SELECT * FROM ORDER_REQUEST WHERE REQUEST_DATE BETWEEN :start AND :end ORDER BY REQUEST_DATE DESC", nativeQuery = true)
    List<OrderRequest> findAllOrderByCycle(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
    @Query(value = "SELECT * FROM ORDER_REQUEST WHERE USER_ID = :userId ORDER BY REQUEST_DATE DESC", nativeQuery = true)
    List<OrderRequest> findAllByUser(@Param("userId") BigInteger userId);
    @Query(value = "SELECT * FROM ORDER_REQUEST WHERE USER_ID = :userId AND ORDER_STATUS <> 'Cancelled' AND ORDER_STATUS <> 'Complete' ORDER BY REQUEST_DATE DESC", nativeQuery = true)
    List<OrderRequest> findAllByUserOpenOnly(@Param("userId") BigInteger userId);
    @Query(value = "SELECT * FROM ORDER_REQUEST WHERE USER_ID = :userId AND ORDER_STATUS = 'Cancelled' OR ORDER_STATUS = 'Complete' ORDER BY REQUEST_DATE DESC", nativeQuery = true)
    List<OrderRequest> findAllByUserNotOpen(@Param("userId") BigInteger userId);
    @Query(value = "SELECT * FROM ORDER_REQUEST WHERE USER_ID = :userId ORDER BY REQUEST_DATE DESC LIMIT 5", nativeQuery = true)
    List<OrderRequest> findFirst5ByUserId(@Param("userId") BigInteger userId);
    @Query(value = "SELECT * FROM ORDER_REQUEST WHERE SUPERVISOR_ID = :supervisorId ORDER BY REQUEST_DATE DESC", nativeQuery = true)
    List<OrderRequest> findAllBySupervisorId(@Param("supervisorId") BigInteger supervisorId);
    @Query(value = "SELECT * FROM ORDER_REQUEST WHERE SUPERVISOR_ID = :supervisorId AND ORDER_STATUS <> 'Cancelled' AND ORDER_STATUS <> 'Complete' ORDER BY REQUEST_DATE DESC", nativeQuery = true)
    List<OrderRequest> findAllBySupervisorIdOpenOnly(@Param("supervisorId") BigInteger supervisorId);
    @Query(value = "SELECT * FROM ORDER_REQUEST_SUPERVISOR_LIST WHERE SUPERVISOR_LIST_ID = :supervisorId", nativeQuery = true)
    List<Tuple> findAllMentionsBySupervisorId(@Param("supervisorId") BigInteger supervisorId);
    @Query(value = "SELECT * FROM ORDER_REQUEST WHERE ID / POWER(10, LENGTH(ID) - LENGTH(:searchTerm)) = :searchTerm", nativeQuery = true)

    List<OrderRequest> searchAllById(@Param("searchTerm") BigInteger searchTerm);

}
