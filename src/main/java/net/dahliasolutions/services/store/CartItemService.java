package net.dahliasolutions.services.store;

import lombok.RequiredArgsConstructor;
import net.dahliasolutions.data.CartItemRepository;
import net.dahliasolutions.models.department.DepartmentRegional;
import net.dahliasolutions.models.store.Cart;
import net.dahliasolutions.models.store.CartItem;
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
    public List<CartItem> findAllByCartAndAndDepartment(Cart cart, DepartmentRegional department) {
        return cartItemRepository.findAllByCartAndAndDepartment(cart, department);
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
