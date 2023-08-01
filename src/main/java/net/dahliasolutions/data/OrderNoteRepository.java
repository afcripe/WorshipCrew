package net.dahliasolutions.data;

import net.dahliasolutions.models.order.OrderNote;
import net.dahliasolutions.models.order.OrderStatus;
import net.dahliasolutions.models.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.math.BigInteger;
import java.util.List;

public interface OrderNoteRepository extends JpaRepository<OrderNote, BigInteger> {

    List<OrderNote> findByOrderId(BigInteger id);
    List<OrderNote> findAllByUser(User user);
    List<OrderNote> findAllByOrderStatus(OrderStatus orderStatus);
}
