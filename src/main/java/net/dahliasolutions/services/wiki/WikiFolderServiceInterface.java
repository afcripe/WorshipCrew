package net.dahliasolutions.services.wiki;

import net.dahliasolutions.models.wiki.WikiFolder;
import net.dahliasolutions.models.wiki.WikiFolderTree;

import java.util.List;
import java.util.Optional;

public interface WikiFolderServiceInterface {

    WikiFolder save(String folder);
    Optional<WikiFolder> findByFolder(String folder);
    List<WikiFolder> findAll();
    List<WikiFolder> findByFolderName(String name);
    void deleteByFolder(WikiFolder wikiFolder);
    WikiFolderTree getFolderTree();

}
