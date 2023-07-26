package net.dahliasolutions.services.wiki;

import lombok.RequiredArgsConstructor;
import net.dahliasolutions.data.WikiImageRepository;
import net.dahliasolutions.models.wiki.WikiImage;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class WikiImageService implements WikiImageServiceInterface {

    private final WikiImageRepository wikiImageRepository;

    @Override
    public WikiImage createStoredImage(WikiImage wikiImage) {
        return wikiImageRepository.save(wikiImage);
    }

    @Override
    public Optional<WikiImage> findById(BigInteger id) {
        return wikiImageRepository.findById(id);
    }

    @Override
    public Optional<WikiImage> findByName(String name) {
        return wikiImageRepository.findByName(name);
    }

    @Override
    public Optional<WikiImage> findByFileLocation(String fileLocation) {
        return wikiImageRepository.findByFileLocation(fileLocation);
    }

    @Override
    public List<WikiImage> findAll() {
        return wikiImageRepository.findAll();
    }

    @Override
    public List<WikiImage> findBySearchTerm(String searchTerm) {
        return wikiImageRepository.findBySearchTerm(searchTerm);
    }

    @Override
    public void save(WikiImage wikiImage) {
        wikiImageRepository.save(wikiImage);
    }

    @Override
    public void deleteById(BigInteger id) {
        wikiImageRepository.deleteById(id);
    }
}
