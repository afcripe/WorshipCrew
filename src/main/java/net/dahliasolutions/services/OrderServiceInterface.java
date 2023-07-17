package net.dahliasolutions.services;

import net.dahliasolutions.models.Order;
import net.dahliasolutions.models.StoreItem;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;

public interface OrderServiceInterface {

    Order createOrder(Order order);
    Optional<Order> findById(BigInteger id);
    List<Order> findAll();
    List<Order> findAllByUser();
    void save(Order order);
    void deleteById(BigInteger id);

}
