package net.dahliasolutions.controllers;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import net.dahliasolutions.models.AppServer;
import net.dahliasolutions.models.User;
import net.dahliasolutions.models.WikiPost;
import net.dahliasolutions.models.WikiTag;
import net.dahliasolutions.services.RedirectService;
import net.dahliasolutions.services.WikiPostService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigInteger;
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
    public String goWikiHome(Model model, HttpSession session) {
        redirectService.setHistory(session, "/wiki");
        List<WikiPost> wikiPostList = wikiPostService.findAll();
        model.addAttribute("wikiPostList", wikiPostList);
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

    @GetMapping("/posts/**")
    public String addNewPost(Model model, HttpServletRequest request, HttpSession session) {

        String requestURL = request.getRequestURL().toString();
        String folderFile = requestURL.split("/posts")[1];
        String[] folderList = folderFile.split("/");
        String postURLName = folderList[folderList.length-1];
        String postName = postURLName.replace("-", " ");
        String folders = "";
        for ( int i=1; i<folderList.length-1; i++ ) {
            folders = folders + "/" + folderList[i];
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
            wikiPost.setFolder("/posts");
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

}
