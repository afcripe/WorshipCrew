package net.dahliasolutions.services;

import net.dahliasolutions.models.Orders;
import net.dahliasolutions.models.User;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;

public interface OrderServiceInterface {

    Orders createOrder(Orders orders);
    Optional<Orders> findById(BigInteger id);
    List<Orders> findAll();
    List<Orders> findAllByUser(User user);
    void save(Orders order);
    void deleteById(BigInteger id);

}