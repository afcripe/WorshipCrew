package net.dahliasolutions.data;

import net.dahliasolutions.models.wiki.WikiPost;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;

public interface WikiPostRepository extends JpaRepository<WikiPost, BigInteger> {

    List<WikiPost> findAllByPublished(Boolean published);
    List<WikiPost> findAllByFolder(String name);
    List<WikiPost> findAllByFolderAndPublished(String name, Boolean published);
    List<WikiPost> findAllByFolderAndPublishedOrderByPinToTopDesc(String name, Boolean published);
    Integer countAllByPublished(Boolean published);

    @Query(value="SELECT * FROM WIKI_POST WHERE UPPER(TITLE) LIKE UPPER(:s) AND PUBLISHED = TRUE", nativeQuery = true)
    List<WikiPost> findAllByTitle(@Param("s") String title);

    @Query(value="SELECT * FROM WIKI_POST WHERE UPPER(TITLE) LIKE UPPER(:s)", nativeQuery = true)
    List<WikiPost> findByTitleIncludeUnpublished(@Param("s") String title);

    @Query(value="SELECT * FROM WIKI_POST WHERE UPPER(TITLE) LIKE CONCAT('%',UPPER(:s),'%') AND PUBLISHED = TRUE", nativeQuery = true)
    List<WikiPost> searchByTitle(@Param("s") String title);

    @Query(value="SELECT * FROM WIKI_POST WHERE UPPER(TITLE) LIKE CONCAT('%',UPPER(:s),'%') OR UPPER(BODY) LIKE CONCAT('%',UPPER(:s),'%') AND PUBLISHED = TRUE", nativeQuery = true)
    List<WikiPost> searchAll(@Param("s") String searchTerm);

    @Query(value="SELECT * FROM WIKI_POST WHERE AUTHOR_ID = :id", nativeQuery = true)
    List<WikiPost> findByAuthor(@Param("id") BigInteger id);

    @Query(value="SELECT * FROM WIKI_POST WHERE AUTHOR_ID = :id AND PUBLISHED = TRUE", nativeQuery = true)
    List<WikiPost> findByAuthorAndPublished(@Param("id") BigInteger id);

    @Query(value="SELECT * FROM WIKI_POST WHERE AUTHOR_ID = :id AND PUBLISHED = FALSE", nativeQuery = true)
    List<WikiPost> findByAuthorAndUnpublished(@Param("id") BigInteger id);


    @Query(value="SELECT * FROM WIKI_POST WHERE PUBLISHED = TRUE ORDER BY LAST_UPDATED DESC LIMIT 15", nativeQuery = true)
    List<WikiPost> findFirst15OrderByLastUpdatedDesc();

    @Query(value="SELECT * FROM WIKI_POST GROUP BY FOLDER AND PUBLISHED = TRUE", nativeQuery = true)
    List<WikiPost> findAllGroupByFolder();

    @Query(value="SELECT WIKI_POST.*, WIKI_POST_TAG_LIST.TAG_LIST_ID FROM WIKI_POST, WIKI_POST_TAG_LIST WHERE WIKI_POST.ID = WIKI_POST_TAG_LIST.WIKI_POST_ID AND TAG_LIST_ID = :id AND PUBLISHED = TRUE", nativeQuery = true)
    List<WikiPost> findAllByTagId(@Param("id") BigInteger id);

    @Query(value="SELECT COUNT(FOLDER) FROM WIKI_POST WHERE FOLDER = :folder AND PUBLISHED = TRUE", nativeQuery = true)
    Optional<Integer> findCountReferencesByFolder(@Param("folder") String folder);

}
