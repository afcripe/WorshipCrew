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
@Entity
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private BigInteger id;
    private BigInteger productId;
    private String productName;
    private String details;
    private int count;              //number of items on hand to send to campus
    private boolean specialOrder;   //not held in stock and must be ordered
    private boolean available;      //available to order
    private int leadTime;
    private LocalDateTime requestDate;

    @ManyToOne(fetch = FetchType.EAGER)
    private DepartmentRegional department;

    @Enumerated(EnumType.STRING)
    private OrderStatus itemStatus;

    @ManyToOne(fetch = FetchType.EAGER)
    private User supervisor;

    @Nullable
    @ManyToOne(fetch = FetchType.LAZY)
    private StoreImage image;

    @JsonManagedReference
    @ManyToOne
    @JoinColumn(name = "orderId")
    private OrderRequest orderRequest;

}
