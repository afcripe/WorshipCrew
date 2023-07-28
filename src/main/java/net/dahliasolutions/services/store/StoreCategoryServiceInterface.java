package net.dahliasolutions.services.store;

import net.dahliasolutions.models.store.StoreCategory;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;

public interface StoreCategoryServiceInterface {

    StoreCategory createCategory(String name);
    Optional<StoreCategory> findById(BigInteger id);
    Optional<StoreCategory> findByName(String name);
    List<StoreCategory> findAll();
    StoreCategory save(StoreCategory category);
    void delete(StoreCategory category);
    void deleteById(BigInteger id);
}
