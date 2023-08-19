package net.dahliasolutions.services.wiki;

import lombok.RequiredArgsConstructor;
import net.dahliasolutions.data.WikiFolderRepository;
import net.dahliasolutions.models.wiki.WikiFolder;
import net.dahliasolutions.models.wiki.WikiFolderTree;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
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
        if (wikiFolder.getFolder().equals("\"/general\"")) {
            return;
        }
        wikiFolderRepository.delete(wikiFolder);
    }

    @Override
    public WikiFolderTree getFolderTree() {
        List<WikiFolder> folders = findAll();
        // convert to array of string
        String[][] dirArray = new String[folders.size()][];
        for (int i=0; i < folders.size(); i++) {
            String[] dirs = folders.get(i).getFolder().substring(1).split("/");
            dirArray[i] = dirs;
        }

        WikiFolderTree tree = new WikiFolderTree();
        tree.setName("/");
        tree.setPath("/");
        tree.setFolders(new ArrayList<>());

        for (int i=0; i < dirArray.length; i++) {
            WikiFolderTree treeFolder = tree;
            String dir = "";

            for (int d=0; d < dirArray[i].length; d++) {
                String currentPath = dirArray[i][d];
                dir = dir+"/"+currentPath;
                Optional<WikiFolderTree> currentNode =
                        treeFolder.getFolders().stream().filter(wikiFolderTree -> wikiFolderTree.getName().equals(currentPath)).findFirst();
                if (currentNode.isEmpty()) {
                    WikiFolderTree node = new WikiFolderTree(currentPath, dir, new ArrayList<>());
                    treeFolder.getFolders().add(node);

                    treeFolder = treeFolder.getFolders().stream().filter(wikiFolderTree -> wikiFolderTree.getName().equals(currentPath)).findFirst().get();
                } else {
                    treeFolder = currentNode.get();
                }
            }
            treeFolder = null;
        }
        return tree;
    }
}
