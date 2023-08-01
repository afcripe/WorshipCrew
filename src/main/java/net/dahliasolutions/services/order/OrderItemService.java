package net.dahliasolutions.services.order;

import lombok.RequiredArgsConstructor;
import net.dahliasolutions.data.OrderItemRepository;
import net.dahliasolutions.models.order.OrderItem;
import net.dahliasolutions.models.order.OrderRequest;
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
        return orderItemRepository.findById(id);
    }

    @Override
    public List<OrderItem> findAllByOrderRequest(OrderRequest orderRequest) {
        return orderItemRepository.findAllByOrderRequest(orderRequest);
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
