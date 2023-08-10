package net.dahliasolutions.models.store;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.annotation.Nullable;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.dahliasolutions.models.department.DepartmentRegional;
import net.dahliasolutions.models.order.OrderStatus;

import java.math.BigInteger;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class CartItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private BigInteger id;
    private BigInteger productId;
    private String productName;
    private String details;
    private int count;              //number of items on hand to send to campus
    private boolean specialOrder;   //not held in stock and must be ordered
    private boolean available;      //available to order
    private int leadTime;           //person who receives order requests

    @ManyToOne(fetch = FetchType.EAGER)
    private DepartmentRegional department;

    @Nullable
    @ManyToOne(fetch = FetchType.LAZY)
    private StoreImage image;

    @JsonManagedReference
    @ManyToOne
    @JoinColumn(name = "cartId")
    private Cart cart;

}
