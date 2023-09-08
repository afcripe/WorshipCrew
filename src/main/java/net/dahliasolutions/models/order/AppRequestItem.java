package net.dahliasolutions.models.order;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.annotation.Nullable;
import jakarta.persistence.*;
import lombok.*;
import net.dahliasolutions.models.department.DepartmentRegional;
import net.dahliasolutions.models.store.StoreImage;
import net.dahliasolutions.models.user.User;

import java.math.BigInteger;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AppRequestItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private BigInteger id;
    private BigInteger productId;
    private String productName;
    private String details;
    private int count;              //number of items on hand to send to campus
    private boolean specialOrder;   //not held in stock and must be ordered
    private boolean available;      //available to order
    private int leadTime;
    private LocalDateTime requestDate;
    private boolean editable;

    private DepartmentRegional department;

    @Enumerated(EnumType.STRING)
    private OrderStatus itemStatus;

    private User supervisor;

    @Nullable
    private StoreImage image;

    public void setAppItemByRequestItem(OrderItem item) {
        this.id = item.getId();
        this.productId = item.getProductId();
        this.productName = item.getProductName();
        this.details = item.getDetails();
        this.count = item.getCount();
        this.specialOrder = item.isSpecialOrder();
        this.available = item.isAvailable();
        this.leadTime = item.getLeadTime();
        this.requestDate = item.getRequestDate();
        this.setEditable(false);
        this.department = item.getDepartment();
        this.itemStatus = item.getItemStatus();
        this.supervisor = item.getSupervisor();
        this.image = item.getImage();
    }
}
