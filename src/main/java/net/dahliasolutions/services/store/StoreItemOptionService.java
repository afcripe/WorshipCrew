package net.dahliasolutions.services.store;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import net.dahliasolutions.data.StoreCategoryRepository;
import net.dahliasolutions.data.StoreItemOptionRepository;
import net.dahliasolutions.data.StoreItemRepository;
import net.dahliasolutions.data.StoreSubCategoryRepository;
import net.dahliasolutions.models.store.StoreCategory;
import net.dahliasolutions.models.store.StoreItem;
import net.dahliasolutions.models.store.StoreItemOption;
import net.dahliasolutions.models.store.StoreSubCategory;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class StoreItemOptionService implements StoreItemOptionServiceInterface {


    private final StoreItemOptionRepository optionRepository;


    @Override
    public Optional<StoreItemOption> findById(BigInteger id) {
        return optionRepository.findById(id);
    }

    @Override
    public List<StoreItemOption> findAll() {
        return optionRepository.findAll();
    }

    @Override
    public StoreItemOption save(StoreItemOption option) {
        return optionRepository.save(option);
    }

    @Override
    public void deleteById(BigInteger id) {
        optionRepository.deleteById(id);
    }
}
