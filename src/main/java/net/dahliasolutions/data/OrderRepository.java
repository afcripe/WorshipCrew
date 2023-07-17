package net.dahliasolutions.data;

import net.dahliasolutions.models.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, BigInteger> {

    List<Order> findAllByUser(BigInteger userId);
}
