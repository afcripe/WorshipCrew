package net.dahliasolutions.models;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.annotation.Nullable;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
    private int count;              //number of items on hand to send to campus
    private boolean specialOrder;   //not held in stock and must be ordered
    private boolean available;      //available to order
    private int leadTime;           //in days

    @Enumerated(EnumType.STRING)
    private OrderStatus itemStatus;

    @Nullable
    @ManyToOne(fetch = FetchType.EAGER)
    private User owner;             //person who receives order requests

    @Nullable
    @ManyToOne(fetch = FetchType.LAZY)
    private StoreImage image;

    @JsonBackReference
    @ManyToOne
    @JoinColumn(name = "cartId")
    private Cart cart;

}
