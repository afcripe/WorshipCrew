package net.dahliasolutions.data;

import net.dahliasolutions.models.Cart;
import net.dahliasolutions.models.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.math.BigInteger;
import java.util.List;

public interface CartItemRepository extends JpaRepository<CartItem, BigInteger> {


}
