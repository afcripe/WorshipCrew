package net.dahliasolutions.services.order;

import net.dahliasolutions.models.order.OrderItem;
import net.dahliasolutions.models.order.OrderRequest;
import net.dahliasolutions.models.store.Cart;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;

public interface OrderItemServiceInterface {

    OrderItem createOrderItem(OrderItem orderItem);
    Optional<OrderItem> findById(BigInteger id);
    List<OrderItem> findAllByOrderRequest(OrderRequest orderRequest);
    void save(OrderItem orderItem);
    void deleteById(BigInteger id);

}
