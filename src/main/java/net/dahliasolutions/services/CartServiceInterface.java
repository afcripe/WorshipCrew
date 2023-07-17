package net.dahliasolutions.services;

import net.dahliasolutions.models.Cart;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;

public interface CartServiceInterface {

    Cart createCart(BigInteger userId);
    Cart findById(BigInteger id);
    Cart save(Cart cart);

}
