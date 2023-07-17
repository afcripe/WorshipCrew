package net.dahliasolutions.models;

import jakarta.persistence.*;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigInteger;
import java.util.Collection;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Transactional
public class Cart {

    @Id
    @Column(name="id")
    private BigInteger id;  // same as user id, only one cart per user
    private int itemCount;

    @OneToMany(fetch = FetchType.EAGER)
    private Collection<CartItem> cartItems;

}
