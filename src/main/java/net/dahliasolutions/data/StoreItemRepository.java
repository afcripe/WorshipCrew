package net.dahliasolutions.data;

import jakarta.persistence.Tuple;
import net.dahliasolutions.models.department.DepartmentRegional;
import net.dahliasolutions.models.position.Position;
import net.dahliasolutions.models.store.StoreCategory;
import net.dahliasolutions.models.store.StoreItem;
import net.dahliasolutions.models.store.StoreSubCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;

public interface StoreItemRepository extends JpaRepository<StoreItem, BigInteger> {

    Optional<StoreItem> findByName(String itemName);
    List<StoreItem> findAll();
    Page<StoreItem> findAll(Pageable pageable);
    Page<StoreItem> findAllByAvailable(boolean available, Pageable pageable);
    Page<StoreItem> findAllByAvailableAndDepartment(boolean available, DepartmentRegional department, Pageable pageable);
    Page<StoreItem> findAllByAvailableAndPositionList(boolean available, Position position, Pageable pageable);
    Page<StoreItem> findAllByAvailableAndDepartmentAndPositionList(boolean available, DepartmentRegional department, Position position, Pageable pageable);

    Page<StoreItem> findAllByAvailableAndCategory(boolean available, StoreCategory category, Pageable pageable);
    Page<StoreItem> findAllByAvailableAndSubCategory(boolean available, StoreSubCategory subCategory, Pageable pageable);
    Page<StoreItem> findAllByAvailableAndCategoryAndPositionList(boolean available, StoreCategory category, Position position, Pageable pageable);
    Page<StoreItem> findAllByAvailableAndCategoryAndDepartment(boolean available, StoreCategory category, DepartmentRegional department, Pageable pageable);
    Page<StoreItem> findAllByAvailableAndCategoryAndDepartmentAndPositionList(boolean available, StoreCategory category, DepartmentRegional department, Position position, Pageable pageable);
    Page<StoreItem> findAllByAvailableAndSubCategoryAndPositionList(boolean available, StoreSubCategory subCategory, Position position, Pageable pageable);
    Page<StoreItem> findAllByAvailableAndSubCategoryAndDepartment(boolean available, StoreSubCategory category, DepartmentRegional department, Pageable pageable);
    Page<StoreItem> findAllByAvailableAndSubCategoryAndDepartmentAndPositionList(boolean available, StoreSubCategory subCategory, DepartmentRegional department, Position position, Pageable pageable);
    List<StoreItem> findBySubCategory(StoreSubCategory subCategory);
    Integer countAllByAvailable(Boolean available);

    @Query(value="SELECT * FROM STORE_ITEM WHERE AVAILABLE = TRUE", nativeQuery = true)
    List<StoreItem> findAllByAvailableNotPaginated();
    @Query(value="SELECT * FROM STORE_ITEM WHERE AVAILABLE = FALSE", nativeQuery = true)
    List<StoreItem> findAllByAvailableNot();
    @Query(value="SELECT * FROM STORE_ITEM WHERE ID = :id AND AVAILABLE = TRUE", nativeQuery = true)
    Optional<StoreItem> findAllByIdAndAvailable(@Param("id") BigInteger id);
    @Query(value="SELECT * FROM STORE_ITEM WHERE ID = :id AND DEPARTMENT_ID = :departmentId AND AVAILABLE = TRUE", nativeQuery = true)
    Optional<StoreItem> findAllByIdAndDepartmentAndAvailable(@Param("id") BigInteger id, @Param("departmentId") BigInteger departmentId);
    @Query(value="SELECT * FROM STORE_ITEM WHERE AVAILABLE = TRUE AND DEPARTMENT_ID = :departmentId", nativeQuery = true)
    List<StoreItem> findAllByAvailableAndDepartmentNotPageable(@Param("departmentId") BigInteger departmentId);
    @Query(value="SELECT * FROM STORE_ITEM WHERE UPPER(NAME) LIKE CONCAT('%',UPPER(:s),'%') OR UPPER(DESCRIPTION) LIKE CONCAT('%',UPPER(:s),'%')", nativeQuery = true)
    List<StoreItem> searchAll(@Param("s") String searchTerm);
    @Query(value="SELECT COUNT(ID) FROM STORE_ITEM WHERE CATEGORY_ID=:i", nativeQuery = true)
    Integer countByCategoryId(@Param("i") BigInteger id);
    @Query(value="SELECT COUNT(ID) FROM STORE_ITEM WHERE SUB_CATEGORY_ID=:i", nativeQuery = true)
    Integer countBySubCategoryId(@Param("i") BigInteger id);
    @Query(value = "SELECT * FROM STORE_ITEM_POSITION_LIST WHERE POSITION_LIST_ID = :positionId", nativeQuery = true)
    List<Tuple> findAllByPosition(@Param("positionId") BigInteger positionId);

    @Query(value="SELECT COUNT(ID) FROM STORE_ITEM WHERE AVAILABLE=:available", nativeQuery = true)
    Integer countByAvailable(@Param("available") Boolean available);

}
