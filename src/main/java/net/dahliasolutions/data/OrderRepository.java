package net.dahliasolutions.data;

import jakarta.persistence.Tuple;
import net.dahliasolutions.models.order.OrderRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigInteger;
import java.util.List;

public interface OrderRepository extends JpaRepository<OrderRequest, BigInteger> {

    @Query(value = "SELECT * FROM ORDER_REQUEST WHERE USER_ID = :userId ORDER BY REQUEST_DATE DESC", nativeQuery = true)
    List<OrderRequest> findAllByUser(@Param("userId") BigInteger userId);
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
