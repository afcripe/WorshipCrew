package net.dahliasolutions.controllers.wiki;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import net.dahliasolutions.models.*;
import net.dahliasolutions.models.user.User;
import net.dahliasolutions.models.wiki.*;
import net.dahliasolutions.services.RedirectService;
import net.dahliasolutions.services.wiki.WikiFolderService;
import net.dahliasolutions.services.wiki.WikiPostService;
import net.dahliasolutions.services.wiki.WikiTagService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigInteger;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/wiki")
@RequiredArgsConstructor
public class WikiController {

    private final WikiPostService wikiPostService;
    private final RedirectService redirectService;
    private final WikiFolderService wikiFolderService;
    private final WikiTagService wikiTagService;
    private final AppServer appServer;

    @ModelAttribute
    public void addAttributes(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) auth.getPrincipal();
        model.addAttribute("moduleTitle", "Wiki");
        model.addAttribute("moduleLink", "/wiki");
        model.addAttribute("userId", user.getId());
        model.addAttribute("baseURL",appServer.getBaseURL());
    }

    @GetMapping("")
    public String getRecent(Model model, HttpSession session) {
        redirectService.setHistory(session, "/wiki");
        List<WikiPost> wikiPostList = wikiPostService.findRecent();
        List<WikiFolder> folderList = wikiFolderService.findAll();

        List<WikiTag> tags = wikiTagService.findAll();
        List<WikiTagReference> tagList = new ArrayList<>();
        for ( WikiTag tag : tags ) {
            WikiTagReference tagCounter = new WikiTagReference();
                tagCounter.setId(tag.getId());
                tagCounter.setName(tag.getName());
                tagCounter.setReferences(wikiTagService.countReferences(tag.getId()));
            tagList.add(tagCounter);
        }


        model.addAttribute("wikiPostList", wikiPostList);
        model.addAttribute("tagList", tagList);
        model.addAttribute("folderList", folderList);
        return "wiki/index";
    }

    @GetMapping("/tag/{id}")
    public String getByTags(@PathVariable BigInteger id, Model model, HttpSession session) {
        redirectService.setHistory(session, "/wiki");
        Optional<WikiTag> wikiTag = wikiTagService.findById(id);
        if (wikiTag.isEmpty()) {
            session.setAttribute("msgError", "Tag not Found.");
            return redirectService.pathName(session, "/wiki");
        }

        List<WikiPost> wikiPostList = wikiPostService.findAllByTagId(id);


        model.addAttribute("wikiPostList", wikiPostList);
        model.addAttribute("tag", wikiTag.get());
        return "wiki/tagPosts";
    }

    @GetMapping("/folder/{name}")
    public String getByTags(@PathVariable String name, Model model, HttpSession session) {
        redirectService.setHistory(session, "/wiki");
        Optional<WikiFolder> wikiFolder = wikiFolderService.findByFolder(name);
        if (wikiFolder.isEmpty()) {
            session.setAttribute("msgError", "Folder not Found.");
            return redirectService.pathName(session, "/wiki");
        }

        List<WikiPost> wikiPostList = wikiPostService.findAllByFolder(name);


        model.addAttribute("wikiPostList", wikiPostList);
        model.addAttribute("folder", wikiFolder.get());
        return "wiki/folderPosts";
    }

    @GetMapping("/group")
    public String getGroupedArticles(Model model, HttpSession session) {
        redirectService.setHistory(session, "/wiki");
        List<WikiPost> wikiPostList = wikiPostService.findAll();
        List<WikiFolder> folders = wikiFolderService.findAll();

        List<GroupedWikiPostList> postList = new ArrayList<>();
        for ( WikiFolder folder : folders ) {
            postList.add(new GroupedWikiPostList(folder.getFolder(), new ArrayList<>()));
        }
        for ( WikiPost post : wikiPostList ) {
            for ( GroupedWikiPostList dir : postList ) {
                if (dir.getFolder().equals(post.getFolder())) {
                    dir.getWikiPost().add(post);
                }
            }
        }

        model.addAttribute("wikiPostList", postList);
        return "wiki/index";
    }

    @GetMapping("/post/{id}")
    public String getPost(@PathVariable("id") BigInteger id, Model model, HttpSession session) {
        Optional<WikiPost> wikiPost = wikiPostService.findById(id);
        if (wikiPost.isEmpty()) {
            session.setAttribute("msgError", "Post not Found.");
            return redirectService.pathName(session, "/wiki");
        }
        model.addAttribute("wikiPost", wikiPost.get());
        return "wiki/post";
    }

    @GetMapping("/articles/**")
    public String addNewPost(Model model, HttpServletRequest request, HttpSession session) {

        String requestURL = request.getRequestURL().toString();
        String folderFile = requestURL.split("/articles")[1];
        String[] folderList = folderFile.split("/");
        String postURLName = folderList[folderList.length-1];
        String postName = postURLName.replace("-", " ");
        String folders = "";
        for ( int i=1; i<folderList.length-1; i++ ) {
            folders = folders + "/" + folderList[i];
        }

        Optional<WikiFolder> dir = wikiFolderService.findByFolder(folderFile);
        if (dir.isPresent()){
            List<WikiPost> wikiPostList = wikiPostService.findAllByFolder(dir.get().getFolder());
            model.addAttribute("wikiPostList", wikiPostList);
            model.addAttribute("folder", dir.get());
            return "wiki/folderPosts";
        }

        List<WikiPost> wikiList = wikiPostService.findByTitle(postName);
        if (wikiList.isEmpty()) {
            session.setAttribute("msgError", "Post not Found.");
            return redirectService.pathName(session, "/wiki");
        }
        if (wikiList.size() > 1) {
            for ( WikiPost w : wikiList ) {
                if ( w.getFolder().equals(folders) ) {
                    model.addAttribute("wikiPost", w);
                    return "wiki/post";
                }
            }
        }

        model.addAttribute("wikiPost", wikiList.get(0));
        return "wiki/post";

    }

    @GetMapping("/post/new")
    public String addNewPost(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) auth.getPrincipal();
        WikiPost wikiPost = new WikiPost();
            wikiPost.setId(BigInteger.valueOf(0));
            wikiPost.setTitle("");
            wikiPost.setBody("");
            wikiPost.setFolder("/general");
            wikiPost.setCreated(LocalDateTime.now());
            wikiPost.setLastUpdated(LocalDateTime.now());
            wikiPost.setSummary("");
            wikiPost.setAuthor(user);
            wikiPost.setTagList(new ArrayList<>());

        model.addAttribute("wikiPost", wikiPost);
        return "wiki/editPost";
    }

    @GetMapping("/post/edit/{id}")
    public String addEditPost(@PathVariable("id") BigInteger id, Model model, HttpSession session) {
        Optional<WikiPost> wikiPost = wikiPostService.findById(id);
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) auth.getPrincipal();

        // if not found, redirect to new post
        if (wikiPost.isEmpty()) {
            return "redirect:/wiki/new/post";
        }
        // validate author
        if (!wikiPost.get().getAuthor().getId().equals(user.getId())) {
            session.setAttribute("msgError", "Edit permission denied.");
            return redirectService.pathName(session, "/wiki");
        }

        model.addAttribute("wikiPost", wikiPost.get());
        return "wiki/editPost";
    }

    @GetMapping("/settings")
    public String getWikiSettings() {
        return "wiki/settings";
    }

    @GetMapping("/tagmanager")
    public String getWikiTagManager(Model model) {
        List<WikiTag> tags = wikiTagService.findAll();
        List<WikiTagReference> tagList = new ArrayList<>();
        for ( WikiTag tag : tags ) {
            WikiTagReference tagCounter = new WikiTagReference();
            tagCounter.setId(tag.getId());
            tagCounter.setName(tag.getName());
            tagCounter.setReferences(wikiTagService.countReferences(tag.getId()));
            tagList.add(tagCounter);
        }

        model.addAttribute("tagList", tagList);
        return "wiki/tagManager";
    }

    @GetMapping("/foldermanager")
    public String getWikiFolderManager(Model model) {
        List<WikiFolder> folderList = wikiFolderService.findAll();
        List<WikiFolderReference> refList = new ArrayList<>();
        for (WikiFolder folder : folderList) {
            WikiFolderReference ref = new WikiFolderReference();
            ref.setFolder(folder.getFolder());
            ref.setReferences(wikiPostService.findCountReferencesByFolder(folder));
            refList.add(ref);
        }
        model.addAttribute("folderList", refList);
        return "wiki/folderManager";
    }

    @GetMapping("/search/{searchTerm}")
    public String searchArticle(@PathVariable String searchTerm, Model model, HttpSession session) {
        redirectService.setHistory(session, "/wiki/search/title/"+searchTerm);
        String searcher = URLDecoder.decode(searchTerm, StandardCharsets.UTF_8);
        List<WikiPost> wikiPostList = wikiPostService.searchAll(searcher);
        List<WikiFolder> folderList = wikiFolderService.findAll();

        List<WikiTag> tags = wikiTagService.findAll();
        List<WikiTagReference> tagList = new ArrayList<>();
        for ( WikiTag tag : tags ) {
            WikiTagReference tagCounter = new WikiTagReference();
            tagCounter.setId(tag.getId());
            tagCounter.setName(tag.getName());
            tagCounter.setReferences(wikiTagService.countReferences(tag.getId()));
            tagList.add(tagCounter);
        }

        model.addAttribute("wikiPostList", wikiPostList);
        model.addAttribute("tagList", tagList);
        model.addAttribute("folderList", folderList);
        model.addAttribute("searchTerm", searchTerm);
        return "wiki/index";
    }
}
