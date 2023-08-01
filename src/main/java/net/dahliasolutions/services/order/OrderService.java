package net.dahliasolutions.services.order;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import net.dahliasolutions.data.OrderItemRepository;
import net.dahliasolutions.data.OrderRepository;
import net.dahliasolutions.data.UserRepository;
import net.dahliasolutions.models.SingleBigIntegerModel;
import net.dahliasolutions.models.order.OrderItem;
import net.dahliasolutions.models.order.OrderNote;
import net.dahliasolutions.models.order.OrderRequest;
import net.dahliasolutions.models.order.OrderStatus;
import net.dahliasolutions.models.store.Cart;
import net.dahliasolutions.models.user.User;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderService implements OrderServiceInterface {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final OrderNoteService orderNoteService;
    private final UserRepository userRepository;

    @Override
    public OrderRequest createOrder(Cart cart) {
        Optional<User> user = userRepository.findById(cart.getId());
        String newNote = "New Order";
        OrderRequest orderRequest = new OrderRequest(
                null,
                LocalDateTime.now(),
                newNote,
                0,
                OrderStatus.Submitted,
                user.get(),
                user.get().getDirector(),
                new ArrayList<>(),
                new ArrayList<>());
        orderRequest = orderRepository.save(orderRequest);
        orderNoteService.createOrderNote(new OrderNote(
                null,
                orderRequest.getId(),
                LocalDateTime.now(),
                newNote,
                OrderStatus.Submitted,
                user.get()));
        return orderRequest;
    }

    @Override
    public Optional<OrderRequest> findById(BigInteger id) {
        Optional<OrderRequest> orderRequest = orderRepository.findById(id);
        if (orderRequest.isPresent()) {
            orderRequest.get().setOrderItems(orderItemRepository.findAllByOrderRequest(orderRequest.get()));
        }
        orderRequest.get().setItemCount(orderRequest.get().getItemCount());
        return orderRequest;
    }

    @Override
    public List<OrderRequest> findAll() {
        List<OrderRequest> orderRequestList = orderRepository.findAll();
        for (OrderRequest orderRequest : orderRequestList) {
            orderRequest.setOrderItems(orderItemRepository.findAllByOrderRequest(orderRequest));
            orderRequest.setItemCount(orderRequest.getItemCount());
        }
        return orderRequestList;
    }

    @Override
    public List<OrderRequest> findAllByUser(User user) {
        List<OrderRequest> orderRequestList = orderRepository.findAllByUser(user);
        for (OrderRequest orderRequest : orderRequestList) {
            orderRequest.setOrderItems(orderItemRepository.findAllByOrderRequest(orderRequest));
            orderRequest.setItemCount(orderRequest.getItemCount());
        }
        return orderRequestList;
    }

    @Override
    public OrderRequest save(OrderRequest orderRequest) {
        for (OrderItem orderItem : orderRequest.getOrderItems()){
            orderItemRepository.save(orderItem);
        }
        return orderRepository.save(orderRequest);
    }

    @Override
    public void deleteById(BigInteger id) {
        Optional<OrderRequest> orderRequest = orderRepository.findById(id);
        if (orderRequest.isPresent()) {
            for (OrderItem orderItem : orderRequest.get().getOrderItems()) {
                orderItemRepository.delete(orderItem);
            }
        }
        orderRepository.deleteById(id);
    }
}
