package net.dahliasolutions.controllers;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import net.dahliasolutions.models.AppServer;
import net.dahliasolutions.models.WikiPost;
import net.dahliasolutions.services.RedirectService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigInteger;
import java.util.Optional;

@Controller
@RequestMapping("/wiki")
@RequiredArgsConstructor
public class WikiController {

    private final RedirectService redirectService;
    private final AppServer appServer;

    @ModelAttribute
    public void addAttributes(Model model) {
        model.addAttribute("moduleTitle", "Wiki");
        model.addAttribute("moduleLink", "/wiki");
    }
    @GetMapping("")
    public String goWikiHome(Model model, HttpSession session) {
        redirectService.setHistory(session, "/wiki");
        return "wiki/index";
    }

    @GetMapping("/admin/post")
    public String addEditPost(Model model, @RequestParam("postId")Optional<BigInteger> postId) {
        setDefaultPost(model);
        model.addAttribute("baseURL",appServer.getBaseURL());
        return "wiki/editPost";
    }

    @PostMapping("/admin/post")
    public String addEditPost(Model model, WikiPost wikiPostModel) {
        System.out.println("Title: "+wikiPostModel.getTitle());
        System.out.println("Body: "+wikiPostModel.getBody());
        return "wiki/editPost";
    }

    private void setDefaultPost(Model model) {
        WikiPost wikiPost = new WikiPost();
        model.addAttribute("wikiPost", wikiPost);
    }

}
