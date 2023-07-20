package net.dahliasolutions.services;

import net.dahliasolutions.models.WikiPost;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;

public interface WikiPostServiceInterface {

    WikiPost createWikiPost(WikiPost wikiPost);
    Optional<WikiPost> findById(BigInteger id);
    List<WikiPost> findByTitle(String title);
    List<WikiPost> searchFirstByTitle(String title);
    List<WikiPost> findAll();
    List<WikiPost> findAllByFolder(String name);
    List<WikiPost> findRecent();
    List<WikiPost> findAllByTagId(BigInteger id);
    List<WikiPost> findByAuthor(BigInteger authorId);
    WikiPost save(WikiPost wikiPost);
    void deleteById(BigInteger id);
}
