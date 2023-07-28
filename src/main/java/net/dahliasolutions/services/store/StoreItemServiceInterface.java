package net.dahliasolutions.services.store;

import net.dahliasolutions.models.store.StoreItem;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;

public interface StoreItemServiceInterface {

    StoreItem createStoreItem(StoreItem storeItem);
    Optional<StoreItem> findById(BigInteger id);
    Optional<StoreItem> findByName(String name);
    List<StoreItem> searchAll(String searchTerm);
    List<StoreItem> findAll();
    void save(StoreItem StoreItem);
    void deleteById(BigInteger id);
    Integer countByCategory(BigInteger id);
    Integer countBySubCategory(BigInteger id);

}
