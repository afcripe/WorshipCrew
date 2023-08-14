package net.dahliasolutions.services.wiki;

import net.dahliasolutions.models.wiki.WikiFolder;
import net.dahliasolutions.models.wiki.WikiPost;
import net.dahliasolutions.models.wiki.WikiTag;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;

public interface WikiPostServiceInterface {

    WikiPost createWikiPost(WikiPost wikiPost);
    Optional<WikiPost> findById(BigInteger id);
    List<WikiPost> findByTitle(String title);
    List<WikiPost> findByTitleIncludeUnpublished(String title);
    List<WikiPost> searchByTitle(String title);
    List<WikiPost> searchAll(String searchTerm);
    List<WikiPost> findAll();
    List<WikiPost> findAllUnpublished();
    List<WikiPost> findAllByFolder(String name);
    List<WikiPost> findRecent();
    List<WikiPost> findAllByTagId(BigInteger id);
    List<WikiPost> findByAuthor(BigInteger authorId);
    List<WikiPost> findByAuthorAndUnpublished(BigInteger authorId);
    WikiPost save(WikiPost wikiPost);
    void mergeTags(WikiTag sourceTag, WikiTag destinationTag);
    void removeTag(WikiTag wikiTag);
    void removeFolder(WikiFolder wikiFolder);
    Integer findCountReferencesByFolder(WikiFolder wikiFolder);
    void updateFolder(String srcFolder, String destFolder);
    void deleteById(BigInteger id);
}
