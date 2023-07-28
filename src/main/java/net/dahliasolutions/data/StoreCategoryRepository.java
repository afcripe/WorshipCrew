package net.dahliasolutions.data;

import net.dahliasolutions.models.store.StoreCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;

public interface StoreCategoryRepository extends JpaRepository<StoreCategory, BigInteger> {

    Optional<StoreCategory> findByName(String name);
}
