package net.dahliasolutions.data;

import net.dahliasolutions.models.order.OrderRequest;
import net.dahliasolutions.models.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.math.BigInteger;
import java.util.List;

public interface OrderRepository extends JpaRepository<OrderRequest, BigInteger> {

    List<OrderRequest> findAllByUser(User user);
}
