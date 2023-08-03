package net.dahliasolutions.services.store;

import lombok.RequiredArgsConstructor;
import net.dahliasolutions.data.StoreItemOptionRepository;
import net.dahliasolutions.data.StoreItemRepository;
import net.dahliasolutions.models.store.StoreItem;
import net.dahliasolutions.models.store.StoreItemOption;
import net.dahliasolutions.models.store.StoreSubCategory;
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
//        List<StoreItemOption> options = optionRepository.findAllByStoreItem(storeItem.get());
//        for (StoreItemOption option : options) {
//            storeItem.get().getItemOptions().add(option);
//        }
        return storeItem;
    }

    @Override
    public Optional<StoreItem> findByName(String name) {
        Optional<StoreItem> storeItem = storeItemRepository.findByName(name);
//        List<StoreItemOption> options = optionRepository.findAllByStoreItem(storeItem.get());
//        for (StoreItemOption option : options) {
//            storeItem.get().getItemOptions().add(option);
//        }
        return storeItem;
    }

    @Override
    public List<StoreItem> searchAll(String searchTerm) {
        List<StoreItem> storeItems = storeItemRepository.searchAll(searchTerm);
//        for (StoreItem item : storeItems) {
//            List<StoreItemOption> options = optionRepository.findAllByStoreItem(item);
//            for (StoreItemOption option : options) {
//                item.getItemOptions().add(option);
//            }
//        }
        return storeItems;
    }


    @Override
    public List<StoreItem> findAll() {
        List<StoreItem> storeItems = storeItemRepository.findAll();
//        for (StoreItem item : storeItems) {
//            List<StoreItemOption> options = optionRepository.findAllByStoreItem(item);
//            for (StoreItemOption option : options) {
//                item.getItemOptions().add(option);
//            }
//        }
        return storeItems;
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
}
