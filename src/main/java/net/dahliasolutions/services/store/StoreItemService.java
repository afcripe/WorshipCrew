package net.dahliasolutions.services.store;

import lombok.RequiredArgsConstructor;
import net.dahliasolutions.data.StoreItemRepository;
import net.dahliasolutions.models.store.StoreItem;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class StoreItemService implements StoreItemServiceInterface {

    private final StoreItemRepository storeItemRepository;

    @Override
    public StoreItem createStoreItem(StoreItem storedImage) {
        return storeItemRepository.save(storedImage);
    }

    @Override
    public Optional<StoreItem> findById(BigInteger id) {
        return storeItemRepository.findById(id);
    }

    @Override
    public Optional<StoreItem> findByName(String name) {
        return storeItemRepository.findByName(name);
    }

    @Override
    public List<StoreItem> searchAll(String searchTerm) {
        return storeItemRepository.searchAll(searchTerm);
    }


    @Override
    public List<StoreItem> findAll() {
        return storeItemRepository.findAll();
    }

    @Override
    public void save(StoreItem StoreItem) {
        storeItemRepository.save(StoreItem);
    }

    @Override
    public void deleteById(BigInteger id) {
        storeItemRepository.deleteById(id);
    }
}
