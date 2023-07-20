package net.dahliasolutions.data;

import net.dahliasolutions.models.WikiTag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigInteger;
import java.util.Optional;

public interface WikiTagRepository extends JpaRepository<WikiTag, BigInteger> {

    Optional<WikiTag> findByName(String name);

    @Query(value="SELECT COUNT(TAGS_LIST_ID) FROM WIKI_POST_TAGS_LIST WHERE TAGS_LIST_ID = :id", nativeQuery = true)
    Optional<Integer> findCountReferencesByTagId(@Param("id") BigInteger id);

}
