package net.dahliasolutions.services.store;

import lombok.RequiredArgsConstructor;
import net.dahliasolutions.data.StoreCategoryRepository;
import net.dahliasolutions.data.StoreItemRepository;
import net.dahliasolutions.data.StoreSubCategoryRepository;
import net.dahliasolutions.models.store.StoreSubCategory;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class StoreSubCategoryService implements StoreSubCategoryServiceInterface {

    private final StoreSubCategoryRepository subCategoryRepository;
    private final StoreItemRepository storeItemRepository;

    @Override
    public StoreSubCategory createCategory(String name) {
        StoreSubCategory subCategory = new StoreSubCategory();
            subCategory.setName(name);
        return subCategoryRepository.save(subCategory);
    }

    @Override
    public Optional<StoreSubCategory> findById(BigInteger id) {
        return subCategoryRepository.findById(id);
    }

    @Override
    public StoreSubCategory save(StoreSubCategory subCategory) {
        return subCategoryRepository.save(subCategory);
    }

    @Override
    public void delete(StoreSubCategory subCategory) {
        subCategoryRepository.delete(subCategory);
    }

    @Override
    public void deleteById(BigInteger id) {
        Integer count = storeItemRepository.countBySubCategoryId(id);
        if (count > 0){
            return;
        }
        subCategoryRepository.deleteById(id);
    }
}
