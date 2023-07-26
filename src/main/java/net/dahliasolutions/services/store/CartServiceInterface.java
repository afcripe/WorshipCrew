package net.dahliasolutions.services.store;

import net.dahliasolutions.models.store.Cart;

import java.math.BigInteger;

public interface CartServiceInterface {

    Cart createCart(BigInteger userId);
    Cart findById(BigInteger id);
    Cart findByUsername(String userName);
    Cart save(Cart cart);
    int getItemCount(BigInteger userId);

}
