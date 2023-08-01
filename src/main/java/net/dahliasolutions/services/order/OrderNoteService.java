package net.dahliasolutions.services.order;

import lombok.RequiredArgsConstructor;
import net.dahliasolutions.data.OrderNoteRepository;
import net.dahliasolutions.models.order.OrderNote;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class OrderNoteService implements OrderNoteServiceInterface {

    private final OrderNoteRepository orderNoteRepository;


    @Override
    public OrderNote createOrderNote(OrderNote orderNote) {
        orderNote.setNoteDate(LocalDateTime.now());
        return orderNoteRepository.save(orderNote);
    }

    @Override
    public Optional<OrderNote> findById(BigInteger id) {
        return orderNoteRepository.findById(id);
    }

    @Override
    public List<OrderNote> findByOrderId(BigInteger orderId) {
        return orderNoteRepository.findByOrderId(orderId);
    }

    @Override
    public void save(OrderNote orderNote) {
        orderNoteRepository.save(orderNote);
    }

    @Override
    public void deleteById(BigInteger id) {
        orderNoteRepository.deleteById(id);
    }
}
