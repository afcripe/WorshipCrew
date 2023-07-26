package net.dahliasolutions.data;

import net.dahliasolutions.models.store.Cart;
import org.springframework.data.jpa.repository.JpaRepository;

import java.math.BigInteger;

public interface CartRepository extends JpaRepository<Cart, BigInteger> {

}
