package net.dahliasolutions.services.order;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import net.dahliasolutions.data.OrderItemRepository;
import net.dahliasolutions.data.OrderRepository;
import net.dahliasolutions.data.UserRepository;
import net.dahliasolutions.models.BrowserMessage;
import net.dahliasolutions.models.campus.Campus;
import net.dahliasolutions.models.mail.EmailDetails;
import net.dahliasolutions.models.order.*;
import net.dahliasolutions.models.store.Cart;
import net.dahliasolutions.models.user.User;
import jakarta.persistence.Tuple;
import net.dahliasolutions.services.mail.EmailService;
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
        String newNote = "New Request";
        OrderRequest orderRequest = new OrderRequest(
                null,
                LocalDateTime.now(),
                newNote,
                0,
                user.get().getCampus(),
                OrderStatus.Submitted,
                user.get(),
                user.get().getDirector(),
                new ArrayList<>(),
                new ArrayList<>());
        orderRequest = orderRepository.save(orderRequest);
        orderNoteService.createOrderNote(new OrderNote(
                null,
                orderRequest.getId(),
                null,
                newNote,
                null,
                OrderStatus.Submitted,
                user.get()));

        return orderRequest;
    }

    @Override
    public Optional<OrderRequest> findById(BigInteger id) {
        Optional<OrderRequest> orderRequest = orderRepository.findById(id);
        if (orderRequest.isPresent()) {
            orderRequest.get().setRequestItems(orderItemRepository.findAllByOrderRequest(orderRequest.get()));
            orderRequest.get().setItemCount(orderRequest.get().getItemCount());
        }
        return orderRequest;
    }

    @Override
    public Optional<OrderRequest> findAllByIdAndCycle(BigInteger id, LocalDateTime startDate, LocalDateTime endDate) {
        return orderRepository.findAllByIdAndCycle(id, startDate, endDate);
    }

    @Override
    public List<OrderRequest> findAll() {
        List<OrderRequest> orderRequestList = orderRepository.findAll();
        for (OrderRequest orderRequest : orderRequestList) {
            orderRequest.setRequestItems(orderItemRepository.findAllByOrderRequest(orderRequest));
            orderRequest.setItemCount(orderRequest.getItemCount());
        }
        return orderRequestList;
    }

    @Override
    public List<OrderRequest> findAllOpen() {
        List<OrderRequest> orderRequestList = orderRepository.findAllOpenOnly();
        for (OrderRequest orderRequest : orderRequestList) {
            orderRequest.setRequestItems(orderItemRepository.findAllByOrderRequest(orderRequest));
            orderRequest.setItemCount(orderRequest.getItemCount());
        }
        return orderRequestList;
    }

    @Override
    public List<OrderRequest> findAllByUser(User user) {
        List<OrderRequest> orderRequestList = orderRepository.findAllByUser(user.getId());
        for (OrderRequest orderRequest : orderRequestList) {
            orderRequest.setRequestItems(orderItemRepository.findAllByOrderRequest(orderRequest));
            orderRequest.setItemCount(orderRequest.getItemCount());
        }
        return orderRequestList;
    }

    @Override
    public List<OrderRequest> findAllByUserOpenOnly(User user) {
        List<OrderRequest> orderRequestList = orderRepository.findAllByUser(user.getId());
        List<OrderRequest> requestList = new ArrayList<>();

        for (OrderRequest r : orderRequestList) {
            if (r.getOrderStatus() != OrderStatus.Complete && r.getOrderStatus() != OrderStatus.Cancelled) {
                requestList.add(r);
            }
        }
        for (OrderRequest orderRequest : orderRequestList) {
            orderRequest.setRequestItems(orderItemRepository.findAllByOrderRequest(orderRequest));
            orderRequest.setItemCount(orderRequest.getItemCount());
        }
        return requestList;
    }

    @Override
    public List<OrderRequest> findAllByUserNotOpen(User user) {
        List<OrderRequest> orderRequestList = orderRepository.findAllByUser(user.getId());
        List<OrderRequest> requestList = new ArrayList<>();

        for (OrderRequest r : orderRequestList) {
            if (r.getOrderStatus() == OrderStatus.Complete || r.getOrderStatus() == OrderStatus.Cancelled) {
                requestList.add(r);
            }
        }

        for (OrderRequest orderRequest : requestList) {
            orderRequest.setRequestItems(orderItemRepository.findAllByOrderRequest(orderRequest));
            orderRequest.setItemCount(orderRequest.getItemCount());
        }
        return requestList;
    }

    @Override
    public List<OrderRequest> findAllByCampus(Campus campus) {
        return orderRepository.findAllByCampus(campus);
    }

    @Override
    public List<OrderRequest> findAllByCampusAndCycle(BigInteger campusId, LocalDateTime startDate, LocalDateTime endDate) {
        return orderRepository.findAllByCampusAndCycle(campusId, startDate, endDate);
    }

    @Override
    public List<OrderRequest> findAllByUserAndCycle(BigInteger userId, LocalDateTime startDate, LocalDateTime endDate) {
        return orderRepository.findAllByUserAndCycle(userId, startDate, endDate);
    }

    @Override
    public List<OrderRequest> findAllBySupervisorAndCycle(BigInteger supervisorId, LocalDateTime startDate, LocalDateTime endDate) {
        return orderRepository.findAllBySupervisorAndCycle(supervisorId, startDate, endDate);
    }

    @Override
    public List<OrderRequest> findFirst5ByUser(User user) {
        List<OrderRequest> orderRequestList = orderRepository.findFirst5ByUserId(user.getId());
        for (OrderRequest orderRequest : orderRequestList) {
            orderRequest.setRequestItems(orderItemRepository.findAllByOrderRequest(orderRequest));
            orderRequest.setItemCount(orderRequest.getItemCount());
        }
        return orderRequestList;
    }

    @Override
    public List<OrderRequest> findAllBySupervisor(User user) {
        List<OrderRequest> orderRequestList = orderRepository.findAllBySupervisorId(user.getId());
        for (OrderRequest orderRequest : orderRequestList) {
            orderRequest.setRequestItems(orderItemRepository.findAllByOrderRequest(orderRequest));
            orderRequest.setItemCount(orderRequest.getItemCount());
        }
        return orderRequestList;
    }

    @Override
    public List<OrderRequest> findAllBySupervisorOpenOnly(User user) {
        List<OrderRequest> orderRequestList = orderRepository.findAllBySupervisorIdOpenOnly(user.getId());
        for (OrderRequest orderRequest : orderRequestList) {
            orderRequest.setRequestItems(orderItemRepository.findAllByOrderRequest(orderRequest));
            orderRequest.setItemCount(orderRequest.getItemCount());
        }
        return orderRequestList;
    }

    @Override
    public List<OrderRequest> findAllByMentionOpenOnly(User user) {
        List<Tuple> orderSupervisorList = orderRepository.findAllMentionsBySupervisorId(user.getId());
        List<OrderRequest> orderRequestList = new ArrayList<>();
        for (Tuple tuple : orderSupervisorList) {
            BigInteger orderId = new BigInteger(tuple.get(0).toString());
            OrderRequest newRequest = orderRepository.findById(orderId).get();
            if (!newRequest.getOrderStatus().equals(OrderStatus.Cancelled)
                    && !newRequest.getOrderStatus().equals(OrderStatus.Complete)) {
                orderRequestList.add(newRequest);
            }
        }
        return orderRequestList;
    }

    @Override
    public List<OrderRequest> findAllByMentionOpenAndCycle(BigInteger supervisorId, LocalDateTime startDate, LocalDateTime endDate) {
        List<Tuple> orderSupervisorList = orderRepository.findAllMentionsBySupervisorId(supervisorId);
        List<OrderRequest> orderRequestList = new ArrayList<>();
        for (Tuple tuple : orderSupervisorList) {
            BigInteger orderId = new BigInteger(tuple.get(0).toString());
            Optional<OrderRequest> newRequest = orderRepository.findAllByIdAndCycle(orderId, startDate, endDate);
            if (newRequest.isPresent()) {
                orderRequestList.add(newRequest.get());
            }
        }
        return orderRequestList;
    }

    @Override
    public List<OrderRequest> searchAllById(BigInteger searchTerm) {
        return orderRepository.searchAllById(searchTerm);
    }

    @Override
    public OrderRequest save(OrderRequest orderRequest) {
        for (OrderItem orderItem : orderRequest.getRequestItems()){
            orderItemRepository.save(orderItem);
        }
        return orderRepository.save(orderRequest);
    }

    @Override
    public void deleteById(BigInteger id) {
        Optional<OrderRequest> orderRequest = orderRepository.findById(id);
        if (orderRequest.isPresent()) {
            for (OrderItem orderItem : orderRequest.get().getRequestItems()) {
                orderItemRepository.delete(orderItem);
            }
        }
        orderRepository.deleteById(id);
    }
}
