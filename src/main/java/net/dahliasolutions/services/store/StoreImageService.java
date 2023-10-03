package net.dahliasolutions.services.store;

import jakarta.persistence.criteria.CriteriaBuilder;
import lombok.RequiredArgsConstructor;
import net.dahliasolutions.data.StoreImageRepository;
import net.dahliasolutions.models.store.StoreImage;
import net.dahliasolutions.models.user.User;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class StoreImageService implements StoreImageServiceInterface {

    private final StoreImageRepository storeImageRepository;

    @Override
    public StoreImage createStoredImage(StoreImage storeImage) {
        return storeImageRepository.save(storeImage);
    }

    @Override
    public Optional<StoreImage> findById(BigInteger id) {
        return storeImageRepository.findById(id);
    }

    @Override
    public Optional<StoreImage> findByName(String name) {
        return storeImageRepository.findByName(name);
    }

    @Override
    public Optional<StoreImage> findByFileLocation(String fileLocation) {
        return storeImageRepository.findByFileLocation(fileLocation);
    }

    @Override
    public List<StoreImage> findAll() {
        List<StoreImage> images = storeImageRepository.findAll();
        Collections.sort(images, new Comparator<StoreImage>() {
            @Override
            public int compare(StoreImage image1, StoreImage image2) {
                return image1.getName().compareToIgnoreCase(image2.getName());
            }
        });
        return images;
    }

    @Override
    public List<StoreImage> findBySearchTerm(String searchTerm) {
        return storeImageRepository.findBySearchTerm(searchTerm);
    }

    @Override
    public void save(StoreImage storeImage) {
        storeImageRepository.save(storeImage);
    }

    @Override
    public void deleteById(BigInteger id) {
        storeImageRepository.deleteById(id);
    }
}
