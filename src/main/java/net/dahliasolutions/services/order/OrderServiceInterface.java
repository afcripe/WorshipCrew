package net.dahliasolutions.services.order;

import net.dahliasolutions.models.order.OrderRequest;
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
    OrderRequest save(OrderRequest orderRequest);
    void deleteById(BigInteger id);

}
