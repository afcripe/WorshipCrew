package net.dahliasolutions.services.wiki;

import net.dahliasolutions.models.wiki.WikiImage;
import net.dahliasolutions.models.wiki.WikiNavigator;

import java.util.List;
import java.util.Optional;

public interface WikiNavigatorServiceInterface {

    WikiNavigator createItem(WikiNavigator navigator);
    List<WikiNavigator> findAll();
    Optional<WikiNavigator> findById(Integer id);
    Optional<WikiNavigator> findByName(String name);
    WikiNavigator save(WikiNavigator navigator);
    void deleteById(Integer id);
}
