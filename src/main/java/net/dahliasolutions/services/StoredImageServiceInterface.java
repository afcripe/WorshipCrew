package net.dahliasolutions.services;

import net.dahliasolutions.models.StoredImage;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;

public interface StoredImageServiceInterface {

    StoredImage createStoredImage(StoredImage storedImage);
    Optional<StoredImage> findById(BigInteger id);
    Optional<StoredImage> findByName(String name);
    Optional<StoredImage> findByFileLocation(String fileLocation);
    List<StoredImage> findAll();
    List<StoredImage> findBySearchTerm(String searchTerm);
    void save(StoredImage storedImage);
    void deleteById(BigInteger id);

}
