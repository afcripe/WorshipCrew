package net.dahliasolutions.services.store;

import net.dahliasolutions.models.department.DepartmentRegional;
import net.dahliasolutions.models.store.Cart;
import net.dahliasolutions.models.store.CartItem;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;

public interface CartItemServiceInterface {

    CartItem createCartItem(CartItem cartItem);
    Optional<CartItem> findById(BigInteger id);
    List<CartItem> findByCart(Cart cart);
    List<CartItem> findAllByCartAndAndDepartment(Cart cart, DepartmentRegional department);
    void save(CartItem cartItem);
    void deleteById(BigInteger id);

}
