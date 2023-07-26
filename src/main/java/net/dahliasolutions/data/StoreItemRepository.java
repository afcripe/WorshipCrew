package net.dahliasolutions.data;

import net.dahliasolutions.models.store.StoreItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;

public interface StoreItemRepository extends JpaRepository<StoreItem, BigInteger> {

    Optional<StoreItem> findByName(String itemName);

    @Query(value="SELECT * FROM STORE_ITEM WHERE UPPER(NAME) LIKE CONCAT('%',UPPER(:s),'%') OR UPPER(DESCRIPTION) LIKE CONCAT('%',UPPER(:s),'%')", nativeQuery = true)
    List<StoreItem> searchAll(@Param("s") String searchTerm);
}
