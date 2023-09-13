package net.dahliasolutions.controllers;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import net.dahliasolutions.models.AdminSettings;
import net.dahliasolutions.models.BrowserMessage;
import net.dahliasolutions.models.mail.EmailDetails;
import net.dahliasolutions.models.LoginModel;
import net.dahliasolutions.models.user.User;
import net.dahliasolutions.models.wiki.WikiFolder;
import net.dahliasolutions.models.wiki.WikiPost;
import net.dahliasolutions.services.*;
import net.dahliasolutions.services.mail.EmailService;
import net.dahliasolutions.services.user.UserService;
import net.dahliasolutions.services.wiki.WikiFolderService;
import net.dahliasolutions.services.wiki.WikiPostService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("")
@RequiredArgsConstructor
public class IndexController {

    private final UserService userService;
    private final EmailService emailService;
    private final RedirectService redirectService;
    private final WikiPostService wikiPostService;
    private final WikiFolderService wikiFolderService;
    private final AdminSettingsService adminSettingsService;

    @ModelAttribute
    public void addAttributes(Model model) {
        model.addAttribute("moduleTitle", "DWC");
        model.addAttribute("moduleLink", "/");
    }

    @GetMapping("/")
    public String getHome(Model model, HttpSession session) {
        redirectService.setHistory(session, "/");
        AdminSettings adminSettings = adminSettingsService.getAdminSettings();
        if (!adminSettings.getPortalHome().equals("")) {
            model.addAttribute("wikiPost", getHomePath(adminSettings.getPortalHome()));
        }
        return "index";
    }

    @GetMapping("/app")
    public String getApp() {
        return "app";
    }
    @GetMapping("/app/**")
    public String redirectToApp() {
        return "app";
    }

    @GetMapping("/login")
    public String loginForm(HttpServletRequest request){
        return "index";
    }

    @GetMapping("/forgotpassword")
    public String resetForm(){
        return "forgotPassword";
    }

    @PostMapping("/signin")
    public String processLogin(HttpSession session) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) auth.getPrincipal();
        session.setAttribute("userDisplayName", user.getFirstName());
        return "index";
    }

    @GetMapping("/logout")
    public String logout() {
        return "logout";
    }

    @PostMapping("/passwordreset")
    public String processPasswordReset(@ModelAttribute LoginModel loginModel, HttpSession session) {
        User user = userService.findByUsername(loginModel.getUsername()).orElse(null);
        if(user == null){
            session.setAttribute("msgError", "Username not Found!");
            return "forgotPassword";
        }
        EmailDetails emailDetails =
                new EmailDetails(user.getContactEmail(),"Password Reset", "", null );

        BrowserMessage returnMsg = emailService.sendPasswordResetMail(emailDetails, user.getId());
        session.setAttribute(returnMsg.getMsgType(), returnMsg.getMessage());
        return "redirect:/";
    }

    private WikiPost getHomePath(String path) {
        String folderFile = path.split("/articles")[1];
        String[] folderList = path.split("/");
        String postURLName = folderList[folderList.length-1];
        String postName = postURLName.replace("-", " ");
        String folders = "";
        for ( int i=1; i<folderList.length-1; i++ ) {
            folders = folders + "/" + folderList[i];
        }

        List<WikiPost> wikiList = wikiPostService.findByTitle(postName);
        if (wikiList.size() > 1) {
            for ( WikiPost w : wikiList ) {
                if ( w.getFolder().equals(folders) ) {
                    return w;
                }
            }
        }

        return wikiList.get(0);
    }

    @GetMapping("/articles/**")
    public String addNewPost(Model model, HttpServletRequest request, HttpSession session) {
        String requestURL = request.getRequestURL().toString();
        String folderFile = requestURL.split("/articles")[1];
        String[] folderList = folderFile.split("/");
        String postURLName = folderList[folderList.length-1];
        String postName = postURLName.replace("-", " ");
        String folders = "";
        WikiPost post = new WikiPost();
                post.setPublished(false);
                post.setAnonymous(false);

        for ( int i=1; i<folderList.length-1; i++ ) {
            folders = folders + "/" + folderList[i];
        }

        Optional<WikiFolder> dir = wikiFolderService.findByFolder(folderFile);
        if (dir.isPresent()){
            session.setAttribute("msgError", "Article not Found.");
            return redirectService.pathName(session, "/");
        }

        List<WikiPost> wikiList = wikiPostService.findByTitle(postName);
        if (wikiList.isEmpty()) {
            session.setAttribute("msgError", "Article not Found.");
            return redirectService.pathName(session, "/");
        }

        if (wikiList.size() > 1) {
            for ( WikiPost w : wikiList ) {
                if ( w.getFolder().equals(folders) ) {
                    post = w;
                    break;
                }
            }
        } else if(wikiList.size() == 1) {
            post = wikiList.get(0);
        }

        if (!post.isAnonymous() || !post.isPublished()) {
            session.setAttribute("msgError", "Access Denied to Article");
            return redirectService.pathName(session, "/");
        }

        model.addAttribute("wikiPost", post);
        return "documentation";

    }

}
