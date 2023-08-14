package net.dahliasolutions.controllers;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import net.dahliasolutions.models.AdminSettings;
import net.dahliasolutions.models.BrowserMessage;
import net.dahliasolutions.models.LoginModel;
import net.dahliasolutions.models.mail.EmailDetails;
import net.dahliasolutions.models.user.User;
import net.dahliasolutions.models.wiki.WikiPost;
import net.dahliasolutions.services.AdminSettingsService;
import net.dahliasolutions.services.RedirectService;
import net.dahliasolutions.services.mail.EmailService;
import net.dahliasolutions.services.user.UserService;
import net.dahliasolutions.services.wiki.WikiPostService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/documentation")
@RequiredArgsConstructor
public class documentationController {

    private final RedirectService redirectService;
    private final AdminSettingsService adminSettingsService;
    private final WikiPostService wikiPostService;

    @ModelAttribute
    public void addAttributes(Model model) {
        model.addAttribute("moduleTitle", "DWC");
        model.addAttribute("moduleLink", "/documentation");
    }

    @GetMapping("")
    public String getDocs(Model model, HttpSession session) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        System.out.println(auth.getPrincipal().toString());
        if (auth.getPrincipal().toString().equals("anonymousUser")) {
            return "redirect:/";
        }

        AdminSettings adminSettings = adminSettingsService.getAdminSettings();
        if (!adminSettings.getDocumentationHome().equals("")) {
            WikiPost w = getDocsHomeFromPath(adminSettings.getDocumentationHome());
            if (!w.isAnonymous()) {
                session.setAttribute("msgError", "Access Denied to Article");
                model.addAttribute("wikiPost", getDefaultDocs());
                return redirectService.pathName(session, "/");
            }
            model.addAttribute("wikiPost", w);
        } else {
            model.addAttribute("wikiPost", getDefaultDocs());
        }

        redirectService.setHistory(session, "/documentation");
        return "documentation";
    }

    private WikiPost getDocsHomeFromPath(String path) {
        String folderFile = path.split("/articles")[1];

        String[] folderList = path.split("/");
        String postURLName = folderList[folderList.length-1];
        String postName = postURLName.replace("-", " ");
        String folders = "";
        WikiPost post = new WikiPost();
                post.setPublished(false);
                post.setAnonymous(false);

        for ( int i=1; i<folderList.length-1; i++ ) {
            folders = folders + "/" + folderList[i];
        }

        List<WikiPost> wikiList = wikiPostService.findByTitle(postName);
        if (wikiList.size() > 1) {
            for ( WikiPost w : wikiList ) {
                if ( w.getFolder().equals(folders) ) {
                    post = w;
                }
            }
        } else if (wikiList.size() == 1) {
            post = wikiList.get(0);
        }

        if (!post.isAnonymous() || !post.isPublished()) {
            post = getDefaultDocs();
        }

        return post;
    }

    private WikiPost getDefaultDocs() {
        String postBody = "<h1>Welcome to the Documentation</h1>" +
                "<p>There is currently no documentation.</p>" +
                "<p>Help documents can be created as new article in resources. " +
                "Once created, update the Documentation home page in resource settings.</p>";

        WikiPost post = new WikiPost();
            post.setBody(postBody);
            post.setPublished(true);
            post.setAnonymous(true);
        return post;
    }
}
