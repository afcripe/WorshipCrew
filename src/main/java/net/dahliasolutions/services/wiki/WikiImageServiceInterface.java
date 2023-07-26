package net.dahliasolutions.services.wiki;

import net.dahliasolutions.models.wiki.WikiImage;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;

public interface WikiImageServiceInterface {

    WikiImage createStoredImage(WikiImage wikiImage);
    Optional<WikiImage> findById(BigInteger id);
    Optional<WikiImage> findByName(String name);
    Optional<WikiImage> findByFileLocation(String fileLocation);
    List<WikiImage> findAll();
    List<WikiImage> findBySearchTerm(String searchTerm);
    void save(WikiImage wikiImage);
    void deleteById(BigInteger id);

}
