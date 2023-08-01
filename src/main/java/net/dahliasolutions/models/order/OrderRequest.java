package net.dahliasolutions.models.order;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.dahliasolutions.models.store.CartItem;
import net.dahliasolutions.models.user.User;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class OrderRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private BigInteger id;
    private LocalDateTime requestDate;
    private String requestNote;
    transient int itemCount;

    @Enumerated(EnumType.STRING)
    private OrderStatus orderStatus;

    @ManyToOne(fetch = FetchType.EAGER)
    private User user;

    @ManyToOne(fetch = FetchType.EAGER)
    private User supervisor;

    @JsonBackReference
    @OneToMany(fetch = FetchType.EAGER, mappedBy = "orderRequest")
    private List<OrderItem> requestItems;

    @JsonBackReference
    @OneToMany(fetch = FetchType.EAGER)
    private List<User> supervisorList;

    public int getItemCount() {
        int counter = 0;
        for (OrderItem item : requestItems) {
            counter = counter+item.getCount();
        }
        return counter;
    }
}
