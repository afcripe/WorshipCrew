package net.dahliasolutions.services.store;

import net.dahliasolutions.models.store.StoreCategory;
import net.dahliasolutions.models.store.StoreSubCategory;

import java.math.BigInteger;
import java.util.Optional;

public interface StoreSubCategoryServiceInterface {

    StoreSubCategory createCategory(String name);
    Optional<StoreSubCategory> findById(BigInteger id);
    Optional<StoreSubCategory> findByName(String name);
    Optional<StoreSubCategory> findByNameAndCategoryId(String name, BigInteger id);
    StoreSubCategory save(StoreSubCategory subCategory);
    void delete(StoreSubCategory subCategory);
    void deleteById(BigInteger id);

}
