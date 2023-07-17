package net.dahliasolutions.data;

import net.dahliasolutions.models.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.math.BigInteger;
import java.util.Optional;

public interface OrderItemRepository extends JpaRepository<OrderItem, BigInteger> {

}
