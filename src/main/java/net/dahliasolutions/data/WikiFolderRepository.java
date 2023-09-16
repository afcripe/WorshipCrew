package net.dahliasolutions.data;

import net.dahliasolutions.models.wiki.WikiFolder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface WikiFolderRepository extends JpaRepository<WikiFolder, String> {

    Optional<WikiFolder> findByFolder(String path);

    @Query(value="SELECT * FROM WIKI_FOLDER ORDER BY FOLDER ASC", nativeQuery = true)
    List<WikiFolder> findAll();

    @Query(value="SELECT * FROM WIKI_FOLDER WHERE LOWER(FOLDER) LIKE CONCAT('%',LOWER(:name),'%')", nativeQuery = true)
    List<WikiFolder> findByFolderName(@Param("name") String name);

    @Query(value="SELECT * FROM WIKI_FOLDER WHERE LOWER(FOLDER) LIKE CONCAT(LOWER(:name),'%')", nativeQuery = true)
    List<WikiFolder> findAllByFolderNameStartsWith(@Param("name") String name);

}
