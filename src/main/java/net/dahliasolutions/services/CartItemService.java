package net.dahliasolutions.services;

import lombok.RequiredArgsConstructor;
import net.dahliasolutions.data.CartItemRepository;
import net.dahliasolutions.models.Cart;
import net.dahliasolutions.models.CartItem;
import net.dahliasolutions.models.OrderItem;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CartItemService implements CartItemServiceInterface {

    private final CartItemRepository cartItemRepository;


    @Override
    public CartItem createCartItem(CartItem cartItem) {
        return cartItemRepository.save(cartItem);
    }

    @Override
    public Optional<CartItem> findById(BigInteger id) {
        return cartItemRepository.findById(id);
    }

    @Override
    public List<CartItem> findByCart(Cart cart) {
        return cartItemRepository.findAllByCart(cart);
    }

    @Override
    public void save(CartItem cartItem) {
        cartItemRepository.save(cartItem);
    }

    @Override
    public void deleteById(BigInteger id) {
        cartItemRepository.deleteById(id);
    }
}
