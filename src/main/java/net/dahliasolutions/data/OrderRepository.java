package net.dahliasolutions.data;

import net.dahliasolutions.models.Orders;
import net.dahliasolutions.models.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.math.BigInteger;
import java.util.List;

public interface OrderRepository extends JpaRepository<Orders, BigInteger> {

    List<Orders> findAllByUser(User user);
}
