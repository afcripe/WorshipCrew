package net.dahliasolutions.services.store;

import lombok.RequiredArgsConstructor;
import net.dahliasolutions.data.OrderItemRepository;
import net.dahliasolutions.models.store.OrderItem;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class OrderItemService implements OrderItemServiceInterface {

    private final OrderItemRepository orderItemRepository;


    @Override
    public OrderItem createOrderItem(OrderItem orderItem) {
        return orderItemRepository.save(orderItem);
    }

    @Override
    public Optional<OrderItem> findById(BigInteger id) {
        return Optional.empty();
    }

    @Override
    public List<OrderItem> findByOrderId(BigInteger orderId) {
        return null;
    }

    @Override
    public List<OrderItem> findByCartId(BigInteger orderId) {
        return null;
    }

    @Override
    public void save(OrderItem orderItem) {
        orderItemRepository.save(orderItem);
    }

    @Override
    public void deleteById(BigInteger id) {
        orderItemRepository.deleteById(id);
    }
}
