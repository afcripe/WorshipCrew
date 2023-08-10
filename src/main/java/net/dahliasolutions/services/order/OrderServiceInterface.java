package net.dahliasolutions.services.order;

import net.dahliasolutions.models.order.OrderNote;
import net.dahliasolutions.models.order.OrderRequest;
import net.dahliasolutions.models.order.OrderStatus;
import net.dahliasolutions.models.store.Cart;
import net.dahliasolutions.models.user.User;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;

public interface OrderServiceInterface {

    OrderRequest createOrder(Cart cart);
    Optional<OrderRequest> findById(BigInteger id);
    List<OrderRequest> findAll();
    List<OrderRequest> findAllByUser(User user);
    List<OrderRequest> findFirst5ByUser(User user);
    List<OrderRequest> findAllBySupervisor(User user);
    List<OrderRequest> findAllBySupervisorOpenOnly(User user);
    List<OrderRequest> findAllByMentionOpenOnly(User user);
    List<OrderRequest> searchAllById(BigInteger searchTerm);
    OrderRequest save(OrderRequest orderRequest);
    void deleteById(BigInteger id);

}
