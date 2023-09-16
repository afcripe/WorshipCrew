package net.dahliasolutions.services.wiki;

import lombok.RequiredArgsConstructor;
import net.dahliasolutions.data.WikiPostRepository;
import net.dahliasolutions.models.wiki.WikiFolder;
import net.dahliasolutions.models.wiki.WikiPost;
import net.dahliasolutions.models.wiki.WikiTag;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class WikiPostService implements WikiPostServiceInterface {

    private final WikiPostRepository wikiPostRepository;

    @Override
    public WikiPost createWikiPost(WikiPost wikiPost) {
        return wikiPostRepository.save(wikiPost);
    }

    @Override
    public Optional<WikiPost> findById(BigInteger id) {
        return wikiPostRepository.findById(id);
    }

    @Override
    public List<WikiPost> findByTitle(String title) {
        return wikiPostRepository.findByTitle(title);
    }

    @Override
    public List<WikiPost> findByTitleIncludeUnpublished(String title) {
        return wikiPostRepository.findByTitleIncludeUnpublished(title);
    }

    @Override
    public List<WikiPost> searchByTitle(String title) {
        return wikiPostRepository.searchByTitle(title);
    }

    @Override
    public List<WikiPost> searchAll(String searchTerm) {
        return wikiPostRepository.searchAll(searchTerm);
    }

    @Override
    public List<WikiPost> findAll() {
        return wikiPostRepository.findAll();
    }

    @Override
    public List<WikiPost> findAllUnpublished() {
        return wikiPostRepository.findAllByPublished(false);
    }

    @Override
    public List<WikiPost> findAllByFolder(String name) {
        return wikiPostRepository.findAllByFolderAndPublished(name, true);
    }

    @Override
    public List<WikiPost> findRecent()  {
        return wikiPostRepository.findFirst15OrderByLastUpdatedDesc();
    }

    @Override
    public List<WikiPost> findAllByTagId(BigInteger id) {
        return wikiPostRepository.findAllByTagId(id);
    }

    @Override
    public List<WikiPost> findByAuthor(BigInteger authorId) {
        return wikiPostRepository.findByAuthor(authorId);
    }

    @Override
    public List<WikiPost> findByAuthorAndUnpublished(BigInteger authorId) {
        return wikiPostRepository.findByAuthorAndUnpublished(authorId);
    }

    @Override
    public List<WikiPost> findByAuthorAndPublished(BigInteger authorId) {
        return wikiPostRepository.findByAuthorAndPublished(authorId);
    }

    @Override
    public WikiPost save(WikiPost wikiPost) {
        return wikiPostRepository.save(wikiPost);
    }

    @Override
    public void mergeTags(WikiTag sourceTag, WikiTag destinationTag) {
        List<WikiPost> postList = wikiPostRepository.findAllByTagId(sourceTag.getId());
        for (WikiPost post : postList) {
            post.getTagList().remove(sourceTag);
            post.getTagList().add(destinationTag);
        }
        wikiPostRepository.saveAll(postList);
    }

    @Override
    public void removeTag(WikiTag wikiTag) {
        List<WikiPost> postList = wikiPostRepository.findAllByTagId(wikiTag.getId());
        for (WikiPost post : postList) {
            post.getTagList().remove(wikiTag);
        }
        wikiPostRepository.saveAll(postList);
    }

    @Override
    public void removeFolder(WikiFolder wikiFolder) {
        List<WikiPost> postList = wikiPostRepository.findAllByFolderAndPublished(wikiFolder.getFolder(), true);
        for (WikiPost post : postList) {
            post.setFolder("/general");
        }
        wikiPostRepository.saveAll(postList);
    }

    @Override
    public Integer findCountReferencesByFolder(WikiFolder wikiFolder) {
        return wikiPostRepository.findCountReferencesByFolder(wikiFolder.getFolder()).orElse(0);
    }

    @Override
    public void updateFolder(String srcFolder, String destFolder) {
        List<WikiPost> postList = wikiPostRepository.findAllByFolderAndPublished(srcFolder, true);
        for (WikiPost post : postList) {
            post.setFolder(destFolder);
        }
        wikiPostRepository.saveAll(postList);
    }

    @Override
    public void deleteById(BigInteger id) {
        wikiPostRepository.deleteById(id);
    }
}
