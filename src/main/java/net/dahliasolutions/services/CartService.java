package net.dahliasolutions.services;

import lombok.RequiredArgsConstructor;
import net.dahliasolutions.data.CartRepository;
import net.dahliasolutions.models.Cart;
import net.dahliasolutions.models.User;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CartService implements CartServiceInterface {

    private final CartRepository cartRepository;
    private final UserService userService;

    @Override
    public Cart createCart(BigInteger userId) {
        Cart cart = new Cart(userId, 0, new ArrayList<>());
        return cartRepository.save(cart);
    }

    @Override
    public Cart findById(BigInteger id) {
        return cartRepository.findById(id).orElse(createCart(id));
    }

    @Override
    public Cart findByUsername(String userName) {
        Optional<User> user = userService.findByUsername(userName);
        return cartRepository.findById(user.get().getId()).orElse(createCart(user.get().getId()));
    }

    @Override
    public Cart save(Cart cart) {
        return cartRepository.save(cart);
    }

    @Override
    public int getItemCount(BigInteger userId) {
        return cartRepository.findById(userId).orElse(createCart(userId)).getItemCount();
    }
}
