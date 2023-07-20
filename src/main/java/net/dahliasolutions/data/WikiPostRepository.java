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

    @Query(value="SELECT * FROM WIKI_POST WHERE UPPER(TITLE) LIKE CONCAT(UPPER(:s))", nativeQuery = true)
    List<WikiPost> findByTitle(@Param("s") String title);

    @Query(value="SELECT * FROM WIKI_POST WHERE UPPER(TITLE) LIKE CONCAT('%',UPPER(:s),'%')", nativeQuery = true)
    List<WikiPost> searchFirstByTitle(@Param("s") String title);

    @Query(value="SELECT * FROM WIKI_POST WHERE AUTHOR_ID = :id", nativeQuery = true)
    List<WikiPost> findByAuthor(@Param("id") BigInteger id);

}
