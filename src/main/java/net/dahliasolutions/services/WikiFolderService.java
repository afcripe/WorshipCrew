package net.dahliasolutions.services;

import lombok.RequiredArgsConstructor;
import net.dahliasolutions.data.WikiFolderRepository;
import net.dahliasolutions.models.WikiFolder;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class WikiFolderService implements WikiFolderServiceInterface {

    private final WikiFolderRepository wikiFolderRepository;

    @Override
    public WikiFolder save(String folder) {
        WikiFolder wikiFolder = new WikiFolder(folder.toLowerCase());
        return wikiFolderRepository.save(wikiFolder);
    }

    @Override
    public Optional<WikiFolder> findByFolder(String folder) {
        return wikiFolderRepository.findByFolder(folder);
    }

    @Override
    public List<WikiFolder> findAll() {
        return wikiFolderRepository.findAll();
    }

    @Override
    public List<WikiFolder> findByFolderName(String name) {
        return wikiFolderRepository.findByFolderName(name);
    }

    @Override
    public void deleteByFolder(WikiFolder wikiFolder) {
        if (wikiFolder.getFolder().equals("\"/general\"")) {return;}
        wikiFolderRepository.delete(wikiFolder);
    }
}
