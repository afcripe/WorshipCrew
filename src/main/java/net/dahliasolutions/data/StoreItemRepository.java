package net.dahliasolutions.data;

import net.dahliasolutions.models.StoreItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.math.BigInteger;
import java.util.Optional;

public interface StoreItemRepository extends JpaRepository<StoreItem, BigInteger> {

    Optional<StoreItem> findByName(String itemName);
}
