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

    @Query(value="SELECT COUNT(ID) FROM STORE_ITEM WHERE CATEGORY_ID=:i", nativeQuery = true)
    Integer countByCategoryId(@Param("i") BigInteger id);

    @Query(value="SELECT COUNT(ID) FROM STORE_ITEM WHERE SUB_CATEGORY_ID=:i", nativeQuery = true)
    Integer countBySubCategoryId(@Param("i") BigInteger id);


}
