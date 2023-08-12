package net.dahliasolutions.data;

import jakarta.persistence.Tuple;
import net.dahliasolutions.models.department.DepartmentRegional;
import net.dahliasolutions.models.position.Position;
import net.dahliasolutions.models.store.StoreItem;
import net.dahliasolutions.models.store.StoreSubCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;

public interface StoreItemRepository extends JpaRepository<StoreItem, BigInteger> {

    Optional<StoreItem> findByName(String itemName);
    @Query(value="SELECT * FROM STORE_ITEM WHERE AVAILABLE = TRUE", nativeQuery = true)
    List<StoreItem> findAllByAvailable();
    @Query(value="SELECT * FROM STORE_ITEM WHERE AVAILABLE = FALSE", nativeQuery = true)
    List<StoreItem> findAllByAvailableNot();
    @Query(value="SELECT * FROM STORE_ITEM WHERE ID = :id AND AVAILABLE = TRUE", nativeQuery = true)
    Optional<StoreItem> findAllByIdAndAvailable(@Param("id") BigInteger id);
    @Query(value="SELECT * FROM STORE_ITEM WHERE ID = :id AND DEPARTMENT_ID = :departmentId AND AVAILABLE = TRUE", nativeQuery = true)
    Optional<StoreItem> findAllByIdAndDepartmentAndAvailable(@Param("id") BigInteger id, @Param("departmentId") BigInteger departmentId);
    @Query(value="SELECT * FROM STORE_ITEM WHERE AVAILABLE = TRUE AND DEPARTMENT_ID = :departmentId", nativeQuery = true)
    List<StoreItem> findAllByAvailableAndDepartment(@Param("departmentId") BigInteger departmentId);
    @Query(value="SELECT * FROM STORE_ITEM WHERE UPPER(NAME) LIKE CONCAT('%',UPPER(:s),'%') OR UPPER(DESCRIPTION) LIKE CONCAT('%',UPPER(:s),'%')", nativeQuery = true)
    List<StoreItem> searchAll(@Param("s") String searchTerm);

    List<StoreItem> findBySubCategory(StoreSubCategory subCategory);

    @Query(value="SELECT COUNT(ID) FROM STORE_ITEM WHERE CATEGORY_ID=:i", nativeQuery = true)
    Integer countByCategoryId(@Param("i") BigInteger id);

    @Query(value="SELECT COUNT(ID) FROM STORE_ITEM WHERE SUB_CATEGORY_ID=:i", nativeQuery = true)
    Integer countBySubCategoryId(@Param("i") BigInteger id);
    @Query(value = "SELECT * FROM STORE_ITEM_POSITION_LIST WHERE POSITION_LIST_ID = :positionId", nativeQuery = true)
    List<Tuple> findAllByPosition(@Param("positionId") BigInteger positionId);

}
