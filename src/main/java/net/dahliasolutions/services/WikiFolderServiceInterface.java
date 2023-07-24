package net.dahliasolutions.services;

import net.dahliasolutions.models.WikiFolder;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;

public interface WikiFolderServiceInterface {

    WikiFolder save(String folder);
    Optional<WikiFolder> findByFolder(String folder);
    List<WikiFolder> findAll();
    List<WikiFolder> findByFolderName(String name);
    void deleteByFolder(WikiFolder wikiFolder);

}
