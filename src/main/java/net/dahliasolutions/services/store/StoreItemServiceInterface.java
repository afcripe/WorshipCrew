package net.dahliasolutions.services.store;

import net.dahliasolutions.models.department.DepartmentRegional;
import net.dahliasolutions.models.position.Position;
import net.dahliasolutions.models.store.StoreItem;
import net.dahliasolutions.models.store.StoreSubCategory;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;

public interface StoreItemServiceInterface {

    StoreItem createStoreItem(StoreItem storeItem);
    Optional<StoreItem> findById(BigInteger id);
    Optional<StoreItem> findByName(String name);
    List<StoreItem> searchAll(String searchTerm);
    List<StoreItem> findAll();
    List<StoreItem> findAllByAvailable();
    List<StoreItem> findAllByAvailableAndPositionListContains(Position position);
    List<StoreItem> findAllByAvailableAndDepartment(BigInteger departmentId);
    List<StoreItem> findAllByAvailableAndPositionListContainsAndDepartment(Position position, BigInteger departmentId);
    List<StoreItem> findBySubCategory(StoreSubCategory subCategory);
    void save(StoreItem storeItem);
    void deleteById(BigInteger id);
    Integer countByCategory(BigInteger id);
    Integer countBySubCategory(BigInteger id);

}
