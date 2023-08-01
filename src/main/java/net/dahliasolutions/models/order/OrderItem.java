package net.dahliasolutions.models.order;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.annotation.Nullable;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.dahliasolutions.models.store.StoreImage;

import java.math.BigInteger;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class OrderItem {

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

    @Nullable
    @ManyToOne(fetch = FetchType.LAZY)
    private StoreImage image;

    @JsonManagedReference
    @ManyToOne
    @JoinColumn(name = "orderId")
    private OrderRequest orderRequest;

}
