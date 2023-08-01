package net.dahliasolutions.services.order;

import net.dahliasolutions.models.order.OrderNote;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;

public interface OrderNoteServiceInterface {

    OrderNote createOrderNote(OrderNote orderNote);
    Optional<OrderNote> findById(BigInteger id);
    List<OrderNote> findByOrderId(BigInteger orderId);
    void save(OrderNote orderNote);
    void deleteById(BigInteger id);

}
