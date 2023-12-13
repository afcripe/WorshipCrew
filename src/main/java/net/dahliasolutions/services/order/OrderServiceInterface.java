package net.dahliasolutions.services.order;

import net.dahliasolutions.models.campus.Campus;
import net.dahliasolutions.models.order.OrderNote;
import net.dahliasolutions.models.order.OrderRequest;
import net.dahliasolutions.models.order.OrderStatus;
import net.dahliasolutions.models.store.Cart;
import net.dahliasolutions.models.user.User;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface OrderServiceInterface {

    OrderRequest createOrder(Cart cart);
    Optional<OrderRequest> findById(BigInteger id);
    Optional<OrderRequest> findAllByIdAndCycle(BigInteger id, LocalDateTime startDate, LocalDateTime endDate);
    List<OrderRequest> findAll();
    List<OrderRequest> findAllOpen();
    List<OrderRequest> findAllByUser(User user);
    List<OrderRequest> findAllByUserOpenOnly(User user);
    List<OrderRequest> findAllByUserNotOpen(User user);
    List<OrderRequest> findAllByCampus(Campus campus);
    List<OrderRequest> findAllByCampusAndCycle(BigInteger campusId, LocalDateTime startDate, LocalDateTime endDate);
    List<OrderRequest> findAllByUserAndCycle(BigInteger userId, LocalDateTime startDate, LocalDateTime endDate);
    List<OrderRequest> findAllBySupervisorAndCycle(BigInteger supervisorId, LocalDateTime startDate, LocalDateTime endDate);
    List<OrderRequest> findFirst5ByUser(User user);
    List<OrderRequest> findAllBySupervisor(User user);
    List<OrderRequest> findAllBySupervisorOpenOnly(User user);
    List<OrderRequest> findAllByMentionOpenOnly(User user);
    List<OrderRequest> findAllByMentionOpenAndCycle(BigInteger supervisorId, LocalDateTime startDate, LocalDateTime endDate);
    List<OrderRequest> searchAllById(BigInteger searchTerm);
    OrderRequest save(OrderRequest orderRequest);
    void deleteById(BigInteger id);

}
