package net.dahliasolutions.models;

import jakarta.annotation.Nullable;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.Collection;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private BigInteger id;
    private LocalDateTime orderDate;

    @Enumerated(EnumType.STRING)
    private OrderStatus orderStatus;

    @Nullable
    @ManyToOne(fetch = FetchType.EAGER)
    private User user;             //person who placed the order request

    @Nullable
    @ManyToOne(fetch = FetchType.EAGER)
    private User manager;             //person who receives order request from user

    @OneToMany
    private Collection<OrderItem> orderItems;

}
