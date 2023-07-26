package net.dahliasolutions.services.store;

import lombok.RequiredArgsConstructor;
import net.dahliasolutions.data.OrderRepository;
import net.dahliasolutions.models.store.Orders;
import net.dahliasolutions.models.user.User;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class OrderService implements OrderServiceInterface {

    private final OrderRepository orderRepository;

    @Override
    public Orders createOrder(Orders orders) {
        return null;
    }

    @Override
    public Optional<Orders> findById(BigInteger id) {
        return Optional.empty();
    }

    @Override
    public List<Orders> findAll() {
        return null;
    }

    @Override
    public List<Orders> findAllByUser(User user) {
        return null;
    }

    @Override
    public void save(Orders orders) {

    }

    @Override
    public void deleteById(BigInteger id) {

    }
}
