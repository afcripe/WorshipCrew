package net.dahliasolutions.data;

import net.dahliasolutions.models.department.DepartmentRegional;
import net.dahliasolutions.models.store.Cart;
import net.dahliasolutions.models.store.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.math.BigInteger;
import java.util.List;

public interface CartItemRepository extends JpaRepository<CartItem, BigInteger> {

    List<CartItem> findAllByCart(Cart cart);
    List<CartItem> findAllByCartAndAndDepartment(Cart cart, DepartmentRegional department);

}
