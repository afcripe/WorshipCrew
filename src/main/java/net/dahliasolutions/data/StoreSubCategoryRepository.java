package net.dahliasolutions.data;

import net.dahliasolutions.models.store.StoreSubCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.math.BigInteger;
import java.util.Optional;

public interface StoreSubCategoryRepository extends JpaRepository<StoreSubCategory, BigInteger> {

    Optional<StoreSubCategory> findByName(String name);
}
