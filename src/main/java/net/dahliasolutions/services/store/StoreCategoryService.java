package net.dahliasolutions.services.store;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import net.dahliasolutions.data.StoreCategoryRepository;
import net.dahliasolutions.data.StoreItemRepository;
import net.dahliasolutions.data.StoreSubCategoryRepository;
import net.dahliasolutions.models.store.StoreCategory;
import net.dahliasolutions.models.store.StoreSubCategory;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class StoreCategoryService implements StoreCategoryServiceInterface {

    private final StoreCategoryRepository categoryRepository;
    private final StoreSubCategoryRepository subCategoryRepository;
    private final StoreItemRepository storeItemRepository;

    @Override
    public StoreCategory createCategory(String name) {
        StoreCategory category = new StoreCategory();
            category.setName(name);
        return categoryRepository.save(category);
    }

    @Override
    public Optional<StoreCategory> findById(BigInteger id) {
        Optional<StoreCategory> category = categoryRepository.findById(id);
        if (category.isPresent()) {
            category.get().setSubCategoryList(subCategoryRepository.findAllByCategoryId(category.get().getId()));
        }
        return category;
    }

    @Override
    public Optional<StoreCategory> findByName(String name) {
        Optional<StoreCategory> category = categoryRepository.findByName(name);
        if (category.isPresent()) {
            category.get().setSubCategoryList(subCategoryRepository.findAllByCategoryId(category.get().getId()));
        }
        return category;
    }

    @Override
    public List<StoreCategory> findAll() {
        List<StoreCategory> categoryList = categoryRepository.findAll();
        for (StoreCategory category : categoryList) {
            category.setSubCategoryList(subCategoryRepository.findAllByCategoryId(category.getId()));
        }
        return categoryList;
    }

    @Override
    public StoreCategory save(StoreCategory category) {
        for (StoreSubCategory sub : category.getSubCategoryList()) {
            subCategoryRepository.save(sub);
        }
        return categoryRepository.save(category);
    }

    @Override
    public void delete(StoreCategory category) {
        // return if store items found in category
        Integer count = storeItemRepository.countByCategoryId(category.getId());
        if (count == 0) {
            return;
        }
        List<StoreSubCategory> subList = category.getSubCategoryList();
        category.getSubCategoryList().removeAll(subList);
        categoryRepository.save(category);
        for (StoreSubCategory sub : subList) {
            subCategoryRepository.delete(sub);
        }
        categoryRepository.delete(category);
    }

    @Override
    public void deleteById(BigInteger id) {
        Integer count = storeItemRepository.countByCategoryId(id);
        if (count > 0) {
            return;
        }

        Optional<StoreCategory> category = categoryRepository.findById(id);
        if (category.isPresent()) {
            for (StoreSubCategory sub : category.get().getSubCategoryList()) {
                subCategoryRepository.deleteById(sub.getId());
            }
        }
        categoryRepository.deleteById(id);
    }
}
