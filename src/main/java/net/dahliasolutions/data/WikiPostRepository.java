package net.dahliasolutions.data;

import net.dahliasolutions.models.StoreImage;
import net.dahliasolutions.models.WikiPost;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;

public interface WikiPostRepository extends JpaRepository<WikiPost, BigInteger> {

    List<WikiPost> findAllByFolder(String name);

    @Query(value="SELECT * FROM WIKI_POST WHERE UPPER(TITLE) LIKE UPPER(:s)", nativeQuery = true)
    List<WikiPost> findByTitle(@Param("s") String title);

    @Query(value="SELECT * FROM WIKI_POST WHERE UPPER(TITLE) LIKE CONCAT('%',UPPER(:s),'%')", nativeQuery = true)
    List<WikiPost> searchFirstByTitle(@Param("s") String title);

    @Query(value="SELECT * FROM WIKI_POST WHERE AUTHOR_ID = :id", nativeQuery = true)
    List<WikiPost> findByAuthor(@Param("id") BigInteger id);


    @Query(value="SELECT * FROM WIKI_POST ORDER BY LAST_UPDATED DESC LIMIT 10", nativeQuery = true)
    List<WikiPost> findFirst10OrderByLastUpdatedDesc();

    @Query(value="SELECT * FROM WIKI_POST GROUP BY FOLDER", nativeQuery = true)
    List<WikiPost> findAllGroupByFolder();

    @Query(value="SELECT WIKI_POST.*, WIKI_POST_TAG_LIST.TAG_LIST_ID FROM WIKI_POST, WIKI_POST_TAG_LIST WHERE WIKI_POST.ID = WIKI_POST_TAG_LIST.WIKI_POST_ID AND TAG_LIST_ID = :id", nativeQuery = true)
    List<WikiPost> findAllByTagId(@Param("id") BigInteger id);

}
