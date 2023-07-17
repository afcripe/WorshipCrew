package net.dahliasolutions.services;

import net.dahliasolutions.models.StoreItem;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;

public interface StoreItemServiceInterface {

    StoreItem createStoreItem(StoreItem storeItem);
    Optional<StoreItem> findById(BigInteger id);
    Optional<StoreItem> findByName(String name);
    List<StoreItem> findAll();
    void save(StoreItem StoreItem);
    void deleteById(BigInteger id);

}
