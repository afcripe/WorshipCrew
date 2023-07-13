package net.dahliasolutions.services;

import lombok.RequiredArgsConstructor;
import net.dahliasolutions.data.StoredImageRepository;
import net.dahliasolutions.models.StoredImage;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class StoredImageService implements StoredImageServiceInterface {

    private final StoredImageRepository storedImageRepository;

    @Override
    public StoredImage createStoredImage(StoredImage storedImage) {
        return storedImageRepository.save(storedImage);
    }

    @Override
    public Optional<StoredImage> findById(BigInteger id) {
        return storedImageRepository.findById(id);
    }

    @Override
    public Optional<StoredImage> findByName(String name) {
        return storedImageRepository.findByName(name);
    }

    @Override
    public Optional<StoredImage> findByFileLocation(String fileLocation) {
        return storedImageRepository.findByFileLocation(fileLocation);
    }

    @Override
    public List<StoredImage> findAll() {
        return storedImageRepository.findAll();
    }

    @Override
    public List<StoredImage> findBySearchTerm(String searchTerm) {
        return storedImageRepository.findBySearchTerm(searchTerm);
    }

    @Override
    public void save(StoredImage storedImage) {
        storedImageRepository.save(storedImage);
    }

    @Override
    public void deleteById(BigInteger id) {
        storedImageRepository.deleteById(id);
    }
}
