package net.dahliasolutions.services.store;

import jakarta.persistence.Tuple;
import lombok.RequiredArgsConstructor;
import net.dahliasolutions.data.StoreItemOptionRepository;
import net.dahliasolutions.data.StoreItemRepository;
import net.dahliasolutions.models.department.DepartmentRegional;
import net.dahliasolutions.models.order.OrderRequest;
import net.dahliasolutions.models.order.OrderStatus;
import net.dahliasolutions.models.position.Position;
import net.dahliasolutions.models.store.StoreCategory;
import net.dahliasolutions.models.store.StoreItem;
import net.dahliasolutions.models.store.StoreItemOption;
import net.dahliasolutions.models.store.StoreSubCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class StoreItemService implements StoreItemServiceInterface {

    private final StoreItemRepository storeItemRepository;
    private final StoreItemOptionRepository optionRepository;

    @Override
    public StoreItem createStoreItem(StoreItem storedImage) {
        return storeItemRepository.save(storedImage);
    }

    @Override
    public Optional<StoreItem> findById(BigInteger id) {
        Optional<StoreItem> storeItem = storeItemRepository.findById(id);
        return storeItem;
    }

    @Override
    public Optional<StoreItem> findByName(String name) {
        Optional<StoreItem> storeItem = storeItemRepository.findByName(name);
        return storeItem;
    }

    @Override
    public List<StoreItem> searchAll(String searchTerm) {
        List<StoreItem> storeItems = storeItemRepository.searchAll(searchTerm);
        return storeItems;
    }

    @Override
    public List<StoreItem> findAll() {
        return storeItemRepository.findAll();
    }

    @Override
    public Page<StoreItem> findAll(Pageable pageable) {
        return storeItemRepository.findAll(pageable);
    }

    @Override
    public List<StoreItem> findAllByAvailableNotPaginated() {
        return storeItemRepository.findAllByAvailableNotPaginated();
    }

    @Override
    public List<StoreItem> findAllByAvailableAndPositionListContains(Position position) {
        // get list of item IDs with position
        List<Tuple> positionList = storeItemRepository.findAllByPosition(position.getId());

        List<StoreItem> storePositionList = new ArrayList<>();
        for (Tuple tuple : positionList) {
            BigInteger itemId = new BigInteger(tuple.get(1).toString());
            Optional<StoreItem> item = storeItemRepository.findAllByIdAndAvailable(itemId);
            if (item.isPresent()) {
                storePositionList.add(item.get());
            }
        }
        return storePositionList;
    }

    @Override
    public List<StoreItem> findAllByAvailableAndDepartmentNotPageable(BigInteger departmentId) {
        return storeItemRepository.findAllByAvailableAndDepartmentNotPageable(departmentId);
    }

    @Override
    public List<StoreItem> findAllByAvailableAndPositionListContainsAndDepartment(Position position, BigInteger departmentId) {
        // get list of item IDs with position
        List<Tuple> positionList = storeItemRepository.findAllByPosition(position.getId());

        List<StoreItem> storePositionList = new ArrayList<>();
        for (Tuple tuple : positionList) {
            BigInteger itemId = new BigInteger(tuple.get(1).toString());
            Optional<StoreItem> item = storeItemRepository.findAllByIdAndDepartmentAndAvailable(itemId, departmentId);
            if (item.isPresent()) {
                storePositionList.add(item.get());
            }
        }
        return storePositionList;
    }

    @Override
    public List<StoreItem> findBySubCategory(StoreSubCategory subCategory) {
        List<StoreItem> storeItems = storeItemRepository.findBySubCategory(subCategory);
        return storeItems;
    }

    @Override
    public void save(StoreItem storeItem) {
        for (StoreItemOption option : storeItem.getItemOptions()){
            optionRepository.save(option);
        }
        storeItemRepository.save(storeItem);
    }

    @Override
    public void deleteById(BigInteger id) {
        Optional<StoreItem> storeItem = storeItemRepository.findById(id);
        if (storeItem.isPresent()) {
            for (StoreItemOption option : storeItem.get().getItemOptions()) {
                optionRepository.delete(option);
            }
        }
        storeItemRepository.deleteById(id);
    }

    @Override
    public Integer countByCategory(BigInteger id) {
        return storeItemRepository.countByCategoryId(id);
    }

    @Override
    public Integer countBySubCategory(BigInteger id) {
        return storeItemRepository.countBySubCategoryId(id);
    }

    @Override
    public Integer countAllByAvailable(Boolean available) {
        return storeItemRepository.countAllByAvailable(available);
    }

    @Override
    public Page<StoreItem> findAllByAvailable(boolean available, Pageable pageable) {
        return storeItemRepository.findAllByAvailable(available, pageable);
    }

    @Override
    public Page<StoreItem> findAllByAvailableAndDepartment(boolean available, DepartmentRegional department, Pageable pageable) {
        return storeItemRepository.findAllByAvailableAndDepartment(available, department, pageable);
    }

    @Override
    public Page<StoreItem> findAllByAvailableAndPositionList(boolean available, Position position, Pageable pageable) {
        return storeItemRepository.findAllByAvailableAndPositionList(available, position, pageable);
    }

    @Override
    public Page<StoreItem> findAllByAvailableAndDepartmentAndPositionList(boolean available, DepartmentRegional department, Position position, Pageable pageable) {
        return storeItemRepository.findAllByAvailableAndDepartmentAndPositionList(available, department, position, pageable);
    }

    @Override
    public Page<StoreItem> findAllByAvailableAndCategory(boolean available, StoreCategory category, Pageable pageable) {
        return storeItemRepository.findAllByAvailableAndCategory(available, category, pageable);
    }

    @Override
    public Page<StoreItem> findAllByAvailableAndSubCategory(boolean available, StoreSubCategory subCategory, Pageable pageable) {
        return storeItemRepository.findAllByAvailableAndSubCategory(available,subCategory,pageable);
    }

    @Override
    public Page<StoreItem> findAllByAvailableAndCategoryAndPositionList(boolean available, StoreCategory category, Position position, Pageable pageable) {
        return storeItemRepository.findAllByAvailableAndCategoryAndPositionList(available, category, position, pageable);
    }

    @Override
    public Page<StoreItem> findAllByAvailableAndCategoryAndDepartment(boolean available, StoreCategory category, DepartmentRegional department, Pageable pageable) {
        return storeItemRepository.findAllByAvailableAndCategoryAndDepartment(available, category, department, pageable);
    }

    @Override
    public Page<StoreItem> findAllByAvailableAndCategoryAndDepartmentAndPositionList(boolean available, StoreCategory category, DepartmentRegional department, Position position, Pageable pageable) {
        return storeItemRepository.findAllByAvailableAndCategoryAndDepartmentAndPositionList(available, category, department, position, pageable);
    }

    @Override
    public Page<StoreItem> findAllByAvailableAndSubCategoryAndPositionList(boolean available, StoreSubCategory subCategory, Position position, Pageable pageable) {
        return storeItemRepository.findAllByAvailableAndSubCategoryAndPositionList(available, subCategory, position, pageable);
    }

    @Override
    public Page<StoreItem> findAllByAvailableAndSubCategoryAndDepartment(boolean available, StoreSubCategory category, DepartmentRegional department, Pageable pageable) {
        return storeItemRepository.findAllByAvailableAndSubCategoryAndDepartment(available, category, department, pageable);
    }

    @Override
    public Page<StoreItem> findAllByAvailableAndSubCategoryAndDepartmentAndPositionList(boolean available, StoreSubCategory subCategory, DepartmentRegional department, Position position, Pageable pageable) {
        return storeItemRepository.findAllByAvailableAndSubCategoryAndDepartmentAndPositionList(available, subCategory, department, position, pageable);
    }
}
