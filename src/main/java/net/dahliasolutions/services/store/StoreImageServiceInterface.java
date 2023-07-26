package net.dahliasolutions.services.store;

import net.dahliasolutions.models.store.StoreImage;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;

public interface StoreImageServiceInterface {

    StoreImage createStoredImage(StoreImage storeImage);
    Optional<StoreImage> findById(BigInteger id);
    Optional<StoreImage> findByName(String name);
    Optional<StoreImage> findByFileLocation(String fileLocation);
    List<StoreImage> findAll();
    List<StoreImage> findBySearchTerm(String searchTerm);
    void save(StoreImage storeImage);
    void deleteById(BigInteger id);

}
