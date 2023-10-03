package net.dahliasolutions.services.order;

import lombok.RequiredArgsConstructor;
import net.dahliasolutions.data.OrderItemRepository;
import net.dahliasolutions.models.department.DepartmentRegional;
import net.dahliasolutions.models.order.OrderItem;
import net.dahliasolutions.models.order.OrderRequest;
import net.dahliasolutions.models.order.OrderStatus;
import net.dahliasolutions.models.user.User;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class OrderItemService implements OrderItemServiceInterface {

    private final OrderItemRepository orderItemRepository;


    @Override
    public OrderItem createOrderItem(OrderItem orderItem) {
        return orderItemRepository.save(orderItem);
    }

    @Override
    public Optional<OrderItem> findById(BigInteger id) {
        return orderItemRepository.findById(id);
    }

    @Override
    public List<OrderItem> findAllByOrderRequest(OrderRequest orderRequest) {
        return orderItemRepository.findAllByOrderRequest(orderRequest);
    }

    @Override
    public List<OrderItem> findAllBySupervisor(User user) {
        return orderItemRepository.findAllBySupervisor(user);
    }

    @Override
    public List<OrderItem> findAllByDepartment(DepartmentRegional department) {
        return orderItemRepository.findAllByDepartment(department);
    }

    @Override
    public List<OrderItem> findAllByDepartmentAndCycle(BigInteger departmentId, LocalDateTime startDate, LocalDateTime endDate) {
        return orderItemRepository.findAllByDepartmentAndCycle(departmentId, startDate, endDate);
    }

    @Override
    public List<OrderItem> findAllBySupervisorAndCycle(BigInteger userId, LocalDateTime startDate, LocalDateTime endDate) {
        return orderItemRepository.findAllBySupervisorAndCycle(userId, startDate, endDate);
    }

    @Override
    public List<OrderItem> findAllBySupervisorOpenOnly(User user) {
        List<OrderItem> itemList = orderItemRepository.findAllBySupervisor(user);
        List<OrderItem> returnItems = new ArrayList<>();
        for (OrderItem item : itemList) {
            if (!item.getItemStatus().equals(OrderStatus.Complete) && !item.getItemStatus().equals(OrderStatus.Cancelled)) {
                returnItems.add(item);
            }
        }
        return returnItems;
    }

    @Override
    public List<OrderItem> findAllByProductId(BigInteger productId) {
        return orderItemRepository.findAllByProductId(productId);
    }

    @Override
    public void save(OrderItem orderItem) {
        orderItemRepository.save(orderItem);
    }

    @Override
    public void deleteById(BigInteger id) {
        orderItemRepository.deleteById(id);
    }
}
