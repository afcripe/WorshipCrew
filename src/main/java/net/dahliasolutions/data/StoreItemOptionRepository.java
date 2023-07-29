package net.dahliasolutions.data;

import net.dahliasolutions.models.store.StoreItem;
import net.dahliasolutions.models.store.StoreItemOption;
import org.springframework.data.jpa.repository.JpaRepository;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;

public interface StoreItemOptionRepository extends JpaRepository<StoreItemOption, BigInteger> {

    Optional<StoreItemOption> findByName(String name);
    List<StoreItemOption> findAllByStoreItem(StoreItem storeItem);
}
