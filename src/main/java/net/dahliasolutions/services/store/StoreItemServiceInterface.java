package net.dahliasolutions.services.store;

import net.dahliasolutions.models.department.DepartmentRegional;
import net.dahliasolutions.models.position.Position;
import net.dahliasolutions.models.store.StoreCategory;
import net.dahliasolutions.models.store.StoreItem;
import net.dahliasolutions.models.store.StoreSubCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;

public interface StoreItemServiceInterface {

    StoreItem createStoreItem(StoreItem storeItem);
    Optional<StoreItem> findById(BigInteger id);
    Optional<StoreItem> findByName(String name);
    List<StoreItem> searchAll(String searchTerm);
    List<StoreItem> findAll();
    Page<StoreItem> findAll(Pageable pageable);
    List<StoreItem> findAllByAvailableNotPaginated();
    List<StoreItem> findAllByAvailableAndPositionListContains(Position position);
    List<StoreItem> findAllByAvailableAndDepartmentNotPageable(BigInteger departmentId);
    List<StoreItem> findAllByAvailableAndPositionListContainsAndDepartment(Position position, BigInteger departmentId);
    List<StoreItem> findBySubCategory(StoreSubCategory subCategory);
    void save(StoreItem storeItem);
    void deleteById(BigInteger id);
    Integer countByCategory(BigInteger id);
    Integer countBySubCategory(BigInteger id);
    Integer countAllByAvailable(Boolean available);

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

}
