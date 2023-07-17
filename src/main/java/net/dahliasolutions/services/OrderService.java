package net.dahliasolutions.services;

import lombok.RequiredArgsConstructor;
import net.dahliasolutions.data.OrderRepository;
import net.dahliasolutions.models.Order;
import net.dahliasolutions.models.StoreItem;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class OrderService implements OrderServiceInterface {

    private final OrderRepository orderRepository;

    @Override
    public Order createOrder(Order order) {
        return null;
    }

    @Override
    public Optional<Order> findById(BigInteger id) {
        return Optional.empty();
    }

    @Override
    public List<Order> findAll() {
        return null;
    }

    @Override
    public List<Order> findAllByUser() {
        return null;
    }

    @Override
    public void save(Order order) {

    }

    @Override
    public void deleteById(BigInteger id) {

    }
}
