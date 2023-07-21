package net.dahliasolutions.services;

import lombok.RequiredArgsConstructor;
import net.dahliasolutions.data.WikiPostRepository;
import net.dahliasolutions.models.WikiPost;
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
    public List<WikiPost> findAllByFolder(String name) {
        return wikiPostRepository.findAllByFolder(name);
    }

    @Override
    public List<WikiPost> findRecent()  {
        return wikiPostRepository.findFirst10OrderByLastUpdatedDesc();
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
    public WikiPost save(WikiPost wikiPost) {
        return wikiPostRepository.save(wikiPost);
    }

    @Override
    public void deleteById(BigInteger id) {
        wikiPostRepository.deleteById(id);
    }
}
