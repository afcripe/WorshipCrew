package net.dahliasolutions.services.store;

import net.dahliasolutions.models.store.StoreCategory;
import net.dahliasolutions.models.store.StoreItem;
import net.dahliasolutions.models.store.StoreItemOption;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;

public interface StoreItemOptionServiceInterface {

    Optional<StoreItemOption> findById(BigInteger id);
    Optional<StoreItemOption> findByName(String name);
    List<StoreItemOption> findAll();
    List<StoreItemOption> findAllByStoreItem(StoreItem storeItem);
    StoreItemOption save(StoreItemOption option);
    void delete(StoreItemOption option);
    void deleteById(BigInteger id);
}
