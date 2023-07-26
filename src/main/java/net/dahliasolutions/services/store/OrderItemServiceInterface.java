package net.dahliasolutions.services.store;

import net.dahliasolutions.models.store.OrderItem;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;

public interface OrderItemServiceInterface {

    OrderItem createOrderItem(OrderItem orderItem);
    Optional<OrderItem> findById(BigInteger id);
    List<OrderItem> findByOrderId(BigInteger orderId);
    List<OrderItem> findByCartId(BigInteger orderId);
    void save(OrderItem orderItem);
    void deleteById(BigInteger id);

}
