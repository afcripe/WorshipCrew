package net.dahliasolutions.models.store;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigInteger;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Entity
public class Cart {

    @Id
    @Column(name="id")
    private BigInteger id;  // same as user id, only one cart per user
    transient int itemCount;

    @OneToMany(fetch = FetchType.EAGER, mappedBy = "cart")
    private List<CartItem> cartItems;

    public int getItemCount() {
        int counter = 0;
        for (CartItem item : cartItems) {
            counter = counter+item.getCount();
        }
        return counter;
    }

}
