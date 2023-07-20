package net.dahliasolutions.controllers;

import lombok.RequiredArgsConstructor;
import net.dahliasolutions.models.*;
import net.dahliasolutions.services.UserService;
import net.dahliasolutions.services.WikiFolderService;
import net.dahliasolutions.services.WikiPostService;
import net.dahliasolutions.services.WikiTagService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.math.BigInteger;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
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

    @PostMapping("/save")
    public BigInteger postWiki(@ModelAttribute WikiPostModel wikiPostModel) {
        Optional<WikiPost> wikiPost = wikiPostService.findById(wikiPostModel.id());
        if (wikiPost.isEmpty()) {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            User user = (User) auth.getPrincipal();
            WikiPost newPost = new WikiPost();
                newPost.setTitle(wikiPostModel.title());
                newPost.setBody(wikiPostModel.body());
                newPost.setFolder("/posts");
        newPost.setCreated(LocalDateTime.now());
        newPost.setLastUpdated(LocalDateTime.now());
                newPost.setAuthor(user);
                newPost.setTagsList(new ArrayList<>());
            return wikiPostService.createWikiPost(newPost).getId();
        }

        wikiPost.get().setTitle(wikiPostModel.title());
        wikiPost.get().setBody(wikiPostModel.body());
        wikiPost.get().setLastUpdated(LocalDateTime.now());

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
}
