package net.dahliasolutions.controllers;

import lombok.RequiredArgsConstructor;
import net.dahliasolutions.models.*;
import net.dahliasolutions.services.WikiFolderService;
import net.dahliasolutions.services.WikiPostService;
import net.dahliasolutions.services.WikiTagService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/wiki")
public class WikiAPIController {

    private final WikiPostService wikiPostService;
    private final WikiFolderService wikiFolderService;
    private final WikiTagService wikiTagService;

    @PostMapping("/save")
    public BigInteger postWiki(@ModelAttribute WikiPostModel wikiPostModel) {
        Optional<WikiPost> wikiPost = wikiPostService.findById(wikiPostModel.id());

        // remove line breaks from summary
        String summary = wikiPostModel.summary().replace("\r\n", " ");

        if (wikiPost.isEmpty()) {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            User user = (User) auth.getPrincipal();
            WikiPost newPost = new WikiPost();
                newPost.setTitle(wikiPostModel.title());
                newPost.setBody(wikiPostModel.body());
                newPost.setFolder("/general");
                newPost.setCreated(LocalDateTime.now());
                newPost.setLastUpdated(LocalDateTime.now());
                newPost.setSummary(summary);
                newPost.setAuthor(user);
                newPost.setTagList(new ArrayList<>());
            return wikiPostService.createWikiPost(newPost).getId();
        }

        wikiPost.get().setTitle(wikiPostModel.title());
        wikiPost.get().setBody(wikiPostModel.body());
        wikiPost.get().setFolder(wikiPostModel.folder());
        wikiPost.get().setLastUpdated(LocalDateTime.now());
        wikiPost.get().setSummary(summary);

        return wikiPostService.save(wikiPost.get()).getId();
    }

    @GetMapping("/folders")
    public List<WikiFolder> getFolders() {
        return wikiFolderService.findAll();
    }

    @PostMapping("/folder")
    public WikiFolder getFolders(@ModelAttribute SingleStringModel stringModel) {
        Optional<WikiFolder> wikiFolder = wikiFolderService.findByFolder(stringModel.name());
        if (wikiFolder.isEmpty()) {
            return wikiFolderService.save(stringModel.name());
        }
        return wikiFolder.get();
    }

    @GetMapping("/tags")
    public List<WikiTag> getTags() {
        return wikiTagService.findAll();
    }

    @PostMapping("/newtag")
    public List<WikiTag> newTag(@ModelAttribute WikiTagModel wikiTagModel) {
        Optional<WikiPost> wikiPost = wikiPostService.findById(wikiTagModel.postId());
        if (wikiPost.isEmpty()) {
            return null;
        }

        Optional<WikiTag> wikiTag = wikiTagService.findByName(wikiTagModel.tagName());
        if (wikiTag.isPresent()) {
            wikiPost.get().getTagList().add(wikiTag.get());
        } else {
            WikiTag newWikiTag = wikiTagService.createWikiTag(new WikiTag(null, wikiTagModel.tagName()));
            wikiPost.get().getTagList().add(newWikiTag);
        }

        List<WikiTag> newList = wikiPostService.save(wikiPost.get()).getTagList();
        return newList;
    }

    @PostMapping("/addtag")
    public List<WikiTag> addTag(@ModelAttribute WikiTagModel wikiTagModel) {
        Optional<WikiPost> wikiPost = wikiPostService.findById(wikiTagModel.postId());
        if (wikiPost.isEmpty()) {
            return null;
        }

        Optional<WikiTag> wikiTag = wikiTagService.findByName(wikiTagModel.tagName());
        if (wikiTag.isPresent()) {
            wikiPost.get().getTagList().add(wikiTag.get());
        }
        List<WikiTag> newList = wikiPostService.save(wikiPost.get()).getTagList();
        return newList;
    }

    @PostMapping("/removetag")
    public List<WikiTag> removeTag(@ModelAttribute WikiTagModel wikiTagModel) {
        Optional<WikiPost> wikiPost = wikiPostService.findById(wikiTagModel.postId());
        if (wikiPost.isEmpty()) {
            return null;
        }

        Optional<WikiTag> wikiTag = wikiTagService.findByName(wikiTagModel.tagName());
        if (wikiTag.isPresent()) {
            wikiPost.get().getTagList().remove(wikiTag.get());
        }
        List<WikiTag> newList = wikiPostService.save(wikiPost.get()).getTagList();
        return newList;
    }

    @PostMapping("/tagmanager/new")
    public WikiTagReference newTagByManager(@ModelAttribute WikiTagReference wikiTagModel) {
        Optional<WikiTag> wikiTag = wikiTagService.findByName(wikiTagModel.getName());

        if (wikiTag.isPresent()) { return null; }

        WikiTag newTag = wikiTagService.createWikiTag(new WikiTag(null, wikiTagModel.getName()));
        WikiTagReference countTag = new WikiTagReference(newTag.getId(), newTag.getName(), 0);
        return countTag;
    }

    @PostMapping("/tagmanager/delete")
    public WikiTagReference deleteTagByManager(@ModelAttribute WikiTagReference wikiTagModel) {
        Optional<WikiTag> wikiTag = wikiTagService.findById(wikiTagModel.getId());
        if (wikiTag.isEmpty()) { return null; }

        wikiPostService.removeTag(wikiTag.get());
        wikiTagService.deleteById(wikiTagModel.getId());

        wikiTagModel.setReferences(0);
        return wikiTagModel;
    }

    @PostMapping("/tagmanager/update")
    public WikiTagReference updateTagByManager(@ModelAttribute WikiTagReference wikiTagModel) {
        Optional<WikiTag> wikiTag = wikiTagService.findByName(wikiTagModel.getName());
        Optional<WikiTag> updateTag = wikiTagService.findById(wikiTagModel.getId());
        if (updateTag.isEmpty()) {
            return null;
        }

        if (wikiTag.isPresent()) {
            wikiPostService.mergeTags(updateTag.get(), wikiTag.get());
            wikiTagService.deleteById(updateTag.get().getId());
            Integer mrgCount = wikiTagService.countReferences(wikiTag.get().getId());
            WikiTagReference mergedTagCount = new WikiTagReference();
                mergedTagCount.setId(wikiTag.get().getId());
                mergedTagCount.setName(wikiTag.get().getName());
                mergedTagCount.setReferences(mrgCount);
            return mergedTagCount;
        }

        if (updateTag.isPresent()) {
            updateTag.get().setName(wikiTagModel.getName());
            wikiTagService.save(updateTag.get());
            return wikiTagModel;
        }
        return null;
    }
}
