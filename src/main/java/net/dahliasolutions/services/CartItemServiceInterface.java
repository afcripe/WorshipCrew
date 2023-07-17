package net.dahliasolutions.services;

import net.dahliasolutions.models.CartItem;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;

public interface CartItemServiceInterface {

    CartItem createCartItem(CartItem cartItem);
    Optional<CartItem> findById(BigInteger id);
    void save(CartItem cartItem);
    void deleteById(BigInteger id);

}
