package net.dahliasolutions.controllers.wiki;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import net.dahliasolutions.models.*;
import net.dahliasolutions.models.user.User;
import net.dahliasolutions.models.user.UserRoles;
import net.dahliasolutions.models.wiki.*;
import net.dahliasolutions.services.AdminSettingsService;
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
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/resource")
@RequiredArgsConstructor
public class WikiController {

    private final WikiPostService wikiPostService;
    private final RedirectService redirectService;
    private final WikiFolderService wikiFolderService;
    private final WikiTagService wikiTagService;
    private final AdminSettingsService adminSettingsService;
    private final AppServer appServer;

    @ModelAttribute
    public void addAttributes(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) auth.getPrincipal();
        model.addAttribute("moduleTitle", "Resource");
        model.addAttribute("moduleLink", "/resource");
        model.addAttribute("userId", user.getId());
        model.addAttribute("baseURL",appServer.getBaseURL());
    }

    @GetMapping("")
    public String getFolderHome(Model model, HttpSession session) {
        AdminSettings adminSettings = adminSettingsService.getAdminSettings();
        if (!adminSettings.getWikiHome().equals("")) {
            return "redirect:/resource/home";
        }

        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        // find parent folders
        List<WikiFolder> folders = wikiFolderService.findAll();
        List<String> parentFolders = new ArrayList<>();
        for (WikiFolder folder : folders) {
            String[] dirs = folder.getFolder().split("/");
            String pFolder = "/"+dirs[1];
            if (!parentFolders.contains(pFolder)) parentFolders.add(pFolder);
        }

        //get article list for each folder

        List<GroupedWikiPostList> folderPosts = new ArrayList<>();
        for (String p : parentFolders) {
            List<WikiFolder> subs = wikiFolderService.findAllByFolderNameStartsWith(p);
            List<WikiFolder> sub = new ArrayList<>();
            for (WikiFolder f : subs) {
                if (!f.getFolder().equals(p)) {
                    String fSubString = f.getFolder().substring(p.length());
                    String[] subDirs = fSubString.split("/");
                    if(subDirs.length <= 2) {
                        sub.add(f);
                    }
                }
            }

            List<WikiPost> posts = wikiPostService.findAllByFolder(p);
            // filter posts for permission
            posts = positionFilterList(user, posts);
            int pinned = 0;
            for (WikiPost post : posts) {
                if (post.isPinToTop()) {
                    pinned++;
                }
            }

            GroupedWikiPostList gp =
                    new GroupedWikiPostList(
                            p.replace("/", ""),
                            posts,
                            posts.size(),
                            sub,
                            pinned);
            folderPosts.add(gp);
        }


        List<WikiTag> tags = wikiTagService.findAll();
        List<WikiTagReference> tagList = new ArrayList<>();
        for ( WikiTag tag : tags ) {
            WikiTagReference tagCounter = new WikiTagReference();
                tagCounter.setId(tag.getId());
                tagCounter.setName(tag.getName());
                tagCounter.setReferencedTag(wikiTagService.countReferences(tag.getId()));
            tagList.add(tagCounter);
        }


        model.addAttribute("pageTitle", "Topics");
        model.addAttribute("wikiPostList", folderPosts);
        model.addAttribute("tagList", tagList);
        model.addAttribute("unpublishedList", wikiPostService.findByAuthorAndUnpublished(user.getId()));
        model.addAttribute("folderTree", wikiFolderService.getFolderTree());
        model.addAttribute("hideInfo", true);
        return "wiki/index";
    }

    @GetMapping("/home")
    public String getRecent(Model model, HttpSession session) {
        AdminSettings adminSettings = adminSettingsService.getAdminSettings();

        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (!adminSettings.getWikiHome().equals("")) {
            List<WikiTag> tags = wikiTagService.findAll();
            List<WikiTagReference> tagList = new ArrayList<>();
            for ( WikiTag tag : tags ) {
                WikiTagReference tagCounter = new WikiTagReference();
                tagCounter.setId(tag.getId());
                tagCounter.setName(tag.getName());
                tagCounter.setReferencedTag(wikiTagService.countReferences(tag.getId()));
                tagList.add(tagCounter);
            }

            model.addAttribute("wikiPost", getWikiFromPath(adminSettings.getWikiHome()));
            model.addAttribute("tagList", tagList);
            model.addAttribute("unpublishedList", wikiPostService.findByAuthorAndUnpublished(user.getId()));
            model.addAttribute("folderTree", wikiFolderService.getFolderTree());

            redirectService.setHistory(session, "/resource");
            return "wiki/wikiHome";
        }

        List<WikiPost> wikiPostList = wikiPostService.findRecent();
        // filter posts for permission
        wikiPostList = positionFilterList(user, wikiPostList);

        List<WikiTag> tags = wikiTagService.findAll();
        List<WikiTagReference> tagList = new ArrayList<>();
        for ( WikiTag tag : tags ) {
            WikiTagReference tagCounter = new WikiTagReference();
            tagCounter.setId(tag.getId());
            tagCounter.setName(tag.getName());
            tagCounter.setReferencedTag(wikiTagService.countReferences(tag.getId()));
            tagList.add(tagCounter);
        }


        model.addAttribute("wikiPostList", wikiPostList);
        model.addAttribute("tagList", tagList);
        model.addAttribute("unpublishedList", wikiPostService.findByAuthorAndUnpublished(user.getId()));
        model.addAttribute("folderTree", wikiFolderService.getFolderTree());
        model.addAttribute("hideInfo", true);
        return "wiki/index";
    }

    @GetMapping("/user")
    public String getUserArticles(Model model, HttpSession session) {
        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        model.addAttribute("wikiPostList", wikiPostService.findByAuthorAndPublished(currentUser.getId()));
        model.addAttribute("unpublishedList", wikiPostService.findByAuthorAndUnpublished(currentUser.getId()));
        return "wiki/userPosts";
    }

    @GetMapping("/tag/{id}")
    public String getByTags(@PathVariable BigInteger id, Model model, HttpSession session) {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Optional<WikiTag> wikiTag = wikiTagService.findById(id);
        if (wikiTag.isEmpty()) {
            session.setAttribute("msgError", "Tag not Found.");
            return redirectService.pathName(session, "/wiki");
        }

        List<WikiPost> wikiPostList = wikiPostService.findAllByTagId(id);
        // filter posts for permission
        wikiPostList = positionFilterList(user, wikiPostList);


        model.addAttribute("wikiPostList", wikiPostList);
        model.addAttribute("tag", wikiTag.get());
        redirectService.setHistory(session, "/resource");
        return "wiki/tagPosts";
    }

    @GetMapping("/folder/{name}")
    public String getByFolder(@PathVariable String name, Model model, HttpSession session) {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        redirectService.setHistory(session, "/resource");
        Optional<WikiFolder> wikiFolder = wikiFolderService.findByFolder("/"+name);
        if (wikiFolder.isEmpty()) {
            session.setAttribute("msgError", "Folder not Found.");
            return redirectService.pathName(session, "/resource");
        }

        List<WikiPost> wikiPostList = wikiPostService.findAllByFolder("/"+name);
        // filter posts for permission
        wikiPostList = positionFilterList(user, wikiPostList);


        model.addAttribute("wikiPostList", wikiPostList);
        model.addAttribute("folder", wikiFolder.get());
        return "wiki/folderPosts";
    }

    @GetMapping("/group")
    public String getGroupedArticles(Model model, HttpSession session) {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        List<WikiPost> wikiPostList = wikiPostService.findAll();
        List<WikiFolder> folders = wikiFolderService.findAll();
        // filter posts for permission
        wikiPostList = positionFilterList(user, wikiPostList);

        List<GroupedWikiPostList> postList = new ArrayList<>();
        for ( WikiFolder folder : folders ) {
            postList.add(new GroupedWikiPostList(folder.getFolder(), new ArrayList<>(), 0, new ArrayList<>(), 0));
        }
        for ( WikiPost post : wikiPostList ) {
            for ( GroupedWikiPostList dir : postList ) {
                if (dir.getFolder().equals(post.getFolder())) {
                    dir.getWikiPost().add(post);
                }
            }
        }

        model.addAttribute("wikiPostList", postList);
        redirectService.setHistory(session, "/resource");
        return "wiki/index";
    }

    @GetMapping("/article/{id}")
    public String getPost(@PathVariable("id") BigInteger id, Model model, HttpSession session) {
        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Optional<WikiPost> wikiPost = wikiPostService.findById(id);
        if (wikiPost.isEmpty()) {
            session.setAttribute("msgError", "Article not Found.");
            return redirectService.pathName(session, "/resource");
        }
        // filter post for permission
        if (!positionFilterPost(currentUser, wikiPost.get())) {
            session.setAttribute("msgError", "Access Denied.");
            return redirectService.pathName(session, "/resource");
        }

        model.addAttribute("wikiPost", wikiPost.get());
        model.addAttribute("wikiPostEditor", postEditor(currentUser, wikiPost.get().getAuthor()));
        return "wiki/post";
    }

    @GetMapping("/articles/**")
    public String addNewPost(Model model, HttpServletRequest request, HttpSession session) {
        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        String requestURL = request.getRequestURL().toString();
        requestURL = requestURL.replace("/articles/articles", "/articles");
        String folderFile = requestURL.split("/articles")[1];
        String[] folderList = folderFile.split("/");
        String postURLName = folderList[folderList.length-1];
        String postName = postURLName.replace("_", " ");
        String folders = "";
        WikiPost post = new WikiPost();
            post.setPublished(false);

        for ( int i=1; i<folderList.length-1; i++ ) {
            folders = folders + "/" + folderList[i];
        }

        Optional<WikiFolder> dir = wikiFolderService.findByFolder(folderFile);
        if (dir.isPresent()){
            if(!articleViewPermission(currentUser)) {
                session.setAttribute("msgError", "Access Denied Resources");
                return redirectService.pathName(session, "/");
            }

            List<GroupedWikiPostList> folderPosts = new ArrayList<>();

            List<WikiFolder> subs = wikiFolderService.findAllByFolderNameStartsWith(dir.get().getFolder());
            List<WikiFolder> sub = new ArrayList<>();
            for (WikiFolder f : subs) {
                if (!f.getFolder().equals(dir.get().getFolder())) {
                    String fSubString = f.getFolder().substring(dir.get().getFolder().length());
                    String[] subDirs = fSubString.split("/");
                    if(subDirs.length <= 2) {
                        sub.add(f);
                    }
                }
            }

            List<WikiPost> wikiPostList = wikiPostService.findAllByFolder(dir.get().getFolder());
            // filter posts for permission
            wikiPostList = positionFilterList(currentUser, wikiPostList);
            int pinned = 0;
            for (WikiPost p : wikiPostList) {
                if (p.isPinToTop()) {
                    pinned++;
                }
            }

            GroupedWikiPostList gp =
                    new GroupedWikiPostList(
                            "",
                            wikiPostList,
                            wikiPostList.size(),
                            sub,
                            pinned);
            folderPosts.add(gp);

            List<WikiTag> tags = wikiTagService.findAll();
            List<WikiTagReference> tagList = new ArrayList<>();
            for ( WikiTag tag : tags ) {
                WikiTagReference tagCounter = new WikiTagReference();
                tagCounter.setId(tag.getId());
                tagCounter.setName(tag.getName());
                tagCounter.setReferencedTag(wikiTagService.countReferences(tag.getId()));
                tagList.add(tagCounter);
            }

            model.addAttribute("pageTitle", dir.get().getFolder());
            model.addAttribute("wikiPostList", folderPosts);
            model.addAttribute("tagList", tagList);
            model.addAttribute("unpublishedList", wikiPostService.findByAuthorAndUnpublished(currentUser.getId()));
            model.addAttribute("folderTree", wikiFolderService.getFolderTree());
            model.addAttribute("hideInfo", true);
            return "wiki/index";
        }

        List<WikiPost> wikiList = wikiPostService.findByTitle(postName);
        if (wikiList.isEmpty()) {
            session.setAttribute("msgError", "Article not Found. It may not be Published.");
            return redirectService.pathName(session, "/resource");
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

        if (!post.isPublished()) {
            session.setAttribute("msgError", "Access Denied to Article, Not Published");
            return redirectService.pathName(session, "/resource");
        }

        if(!articleViewPermission(currentUser)) {
            if(!post.isAnonymous()) {
                session.setAttribute("msgError", "Access Denied to Article, Not Published to Documentation");
                return redirectService.pathName(session, "/resource");
            }
        }

        model.addAttribute("wikiPost", post);
        model.addAttribute("wikiPostEditor", postEditor(currentUser, post.getAuthor()));
        return "wiki/post";

    }

    @GetMapping("/new")
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
            wikiPost.setAnonymous(false);
            wikiPost.setPublished(false);
            wikiPost.setHideInfo(false);
            wikiPost.setPinToTop(false);
            wikiPost.setTagList(new ArrayList<>());
            wikiPost.setPositionList(new ArrayList<>());

        model.addAttribute("wikiPost", wikiPost);
        return "wiki/editPost";
    }

    @GetMapping("/edit/{id}")
    public String addEditPost(@PathVariable("id") BigInteger id, Model model, HttpSession session) {
        Optional<WikiPost> wikiPost = wikiPostService.findById(id);
        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        // if not found, redirect to new post
        if (wikiPost.isEmpty()) {
            return "redirect:/resource/article/new";
        }
        // validate author
        if (!postEditor(currentUser, wikiPost.get().getAuthor())) {
            session.setAttribute("msgError", "Edit permission denied.");
            return redirectService.pathName(session, "/resource");
        }

        model.addAttribute("wikiPost", wikiPost.get());
        return "wiki/editPost";
    }

    @GetMapping("/settings")
    public String getWikiSettings(Model model) {
        model.addAttribute("publishedCount", wikiPostService.countAllByPublished(true));
        model.addAttribute("unpublishedCount", wikiPostService.countAllByPublished(false));
        model.addAttribute("wikiHome", adminSettingsService.getAdminSettings().getWikiHome());
        model.addAttribute("portalHome", adminSettingsService.getAdminSettings().getPortalHome());
        model.addAttribute("docsHome", adminSettingsService.getAdminSettings().getDocumentationHome());
        model.addAttribute("unpublishedList", wikiPostService.findAllUnpublished());
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
            tagCounter.setReferencedTag(wikiTagService.countReferences(tag.getId()));
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
            ref.setReferencedFolder(wikiPostService.findCountReferencesByFolder(folder));
            refList.add(ref);
        }
        model.addAttribute("folderList", refList);
        return "wiki/folderManager";
    }

    @GetMapping("/recent")
    public String getRecentArticles(Model model, HttpSession session) {
        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        List<WikiPost> wikiPostList = wikiPostService.findRecent();
        // filter posts for permission
        wikiPostList = positionFilterList(currentUser, wikiPostList);

        model.addAttribute("wikiPostList", wikiPostList);
        model.addAttribute("searchTerm", "");
        redirectService.setHistory(session, "/resource/recent");
        return "wiki/searchResults";
    }

    @GetMapping("/search/{searchTerm}")
    public String searchArticle(@PathVariable String searchTerm, Model model, HttpSession session) {
        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        redirectService.setHistory(session, "/resource/search/title/"+searchTerm);
        String searcher = URLDecoder.decode(searchTerm, StandardCharsets.UTF_8);

        List<WikiPost> wikiPostList = wikiPostService.searchAll(searcher);
        // filter posts for permission
        wikiPostList = positionFilterList(currentUser, wikiPostList);

        List<WikiTag> tags = wikiTagService.findAll();
        List<WikiTagReference> tagList = new ArrayList<>();
        for ( WikiTag tag : tags ) {
            WikiTagReference tagCounter = new WikiTagReference();
            tagCounter.setId(tag.getId());
            tagCounter.setName(tag.getName());
            tagCounter.setReferencedTag(wikiTagService.countReferences(tag.getId()));
            tagList.add(tagCounter);
        }

        model.addAttribute("wikiPostList", wikiPostList);
        model.addAttribute("searchTerm", searcher);
        return "wiki/searchResults";
    }

    @GetMapping("/folders")
    public String loadFolderTree(Model model) {
        List<WikiFolder> folders = wikiFolderService.findAll();
        String[] folderArray = new String[folders.size()];
        for (int i=0; i<folders.size(); i++) {
            folderArray[i] = folders.get(i).getFolder();
        }
        model.addAttribute("folderList", folderArray);
        return "wiki/folderDialog";
    }

    private WikiPost getWikiFromPath(String path) {
        String folderFile = path.split("/articles")[1];

        String[] folderList = path.split("/");
        String postURLName = folderList[folderList.length-1];
        String postName = postURLName.replace("_", " ");
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


    private boolean postEditor(User currentUser){
        Collection<UserRoles> roles = currentUser.getUserRoles();
        for (UserRoles role : roles){
            if (role.getName().equals("ADMIN_WRITE") || role.getName().equals("RESOURCE_SUPERVISOR")) {
                return true;
            }
        }
        return false;
    }
    private boolean postEditor(User currentUser, User author){
        if(currentUser.getId().equals(author.getId())) { return true; }

        Collection<UserRoles> roles = currentUser.getUserRoles();
        for (UserRoles role : roles){
            if (role.getName().equals("ADMIN_WRITE") || role.getName().equals("RESOURCE_SUPERVISOR")) {
                return true;
            }
        }
        return false;
    }

    private boolean articleViewPermission(User currentUser){
        Collection<UserRoles> roles = currentUser.getUserRoles();
        for (UserRoles role : roles){
            if (role.getName().equals("ADMIN_WRITE") || role.getName().equals("RESOURCE_SUPERVISOR")
                    || role.getName().equals("RESOURCE_WRITE") || role.getName().equals("RESOURCE_READ")) {
                return true;
            }
        }
        return false;
    }

    private boolean positionFilterPost(User currentUser, WikiPost post) {
        Collection<UserRoles> roles = currentUser.getUserRoles();
        for (UserRoles role : roles){
            if (role.getName().equals("ADMIN_WRITE") || role.getName().equals("RESOURCE_SUPERVISOR")) {
                return true;
            }
        }
        if (post.getPositionList().isEmpty()) {
            return true;
        }
        if (post.getPositionList().contains(currentUser.getPosition())) {
            return true;
        }
        if (post.getAuthor().getId().equals(currentUser.getId())) {
            return true;
        }
        return false;
    }

    private List<WikiPost> positionFilterList(User currentUser, List<WikiPost> posts) {
        List<WikiPost> returnList = new ArrayList<>();

        Collection<UserRoles> roles = currentUser.getUserRoles();
        for (WikiPost post : posts) {
            for (UserRoles role : roles){
                if (role.getName().equals("ADMIN_WRITE") || role.getName().equals("RESOURCE_SUPERVISOR")) {
                    if (!returnList.contains(post)) {
                        returnList.add(post);
                    }
                }
            }
            if (post.getPositionList().isEmpty()) {
                if (!returnList.contains(post)) {
                    returnList.add(post);
                }
            }
            if (post.getPositionList().contains(currentUser.getPosition())) {
                if (!returnList.contains(post)) {
                    returnList.add(post);
                }
            }
            if (post.getAuthor().getId().equals(currentUser.getId())) {
                if (!returnList.contains(post)) {
                    returnList.add(post);
                }
            }
        }
        return returnList;
    }
}
