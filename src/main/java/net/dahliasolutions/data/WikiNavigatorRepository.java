package net.dahliasolutions.data;

import net.dahliasolutions.models.wiki.WikiImage;
import net.dahliasolutions.models.wiki.WikiNavigator;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;

public interface WikiNavigatorRepository extends JpaRepository<WikiNavigator, Integer> {

    @Query(value="SELECT * FROM WIKI_NAVIGATOR ORDER BY ITEM_ORDER ASC", nativeQuery = true)
    List<WikiNavigator> findAllOrderByItemOrder();
    Optional<WikiNavigator> findByName(String name);
    Optional<WikiNavigator> findByLinkLocation(String linkLocation);
}
