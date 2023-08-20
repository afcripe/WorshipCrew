package net.dahliasolutions.services.wiki;

import lombok.RequiredArgsConstructor;
import net.dahliasolutions.data.WikiTagRepository;
import net.dahliasolutions.models.wiki.WikiTag;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class WikiTagService implements WikiTagServiceInterface{

    private final WikiTagRepository wikiTagRepository;

    @Override
    public WikiTag createWikiTag(WikiTag wikiTag) {
        wikiTag.setName(wikiTag.getName().toLowerCase());
        return wikiTagRepository.save(wikiTag);
    }

    @Override
    public Optional<WikiTag> findById(BigInteger id) {
        return wikiTagRepository.findById(id);
    }

    @Override
    public Optional<WikiTag> findByName(String name) {
        return wikiTagRepository.findByName(name);
    }

    @Override
    public List<WikiTag> findAll() {
        return wikiTagRepository.findAll();
    }

    @Override
    public void save(WikiTag wikiTag) {
        wikiTag.setName(wikiTag.getName().toLowerCase());
        wikiTagRepository.save(wikiTag);
    }

    @Override
    public void deleteById(BigInteger id) {
//        wikiTagRepository.deleteReferenceById(id);
        wikiTagRepository.deleteById(id);
    }

    @Override
    public Integer countReferences(BigInteger id) {
        return wikiTagRepository.findCountReferencesByTagId(id).orElse(0);
    }
}
