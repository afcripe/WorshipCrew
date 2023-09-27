package net.dahliasolutions.controllers.app;

import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import net.dahliasolutions.models.APIUser;
import net.dahliasolutions.models.records.*;
import net.dahliasolutions.models.user.User;
import net.dahliasolutions.models.wiki.*;
import net.dahliasolutions.services.JwtService;
import net.dahliasolutions.services.user.UserService;
import net.dahliasolutions.services.wiki.WikiFolderService;
import net.dahliasolutions.services.wiki.WikiPostService;
import net.dahliasolutions.services.wiki.WikiTagService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigInteger;
import java.util.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/app/resources")
public class MobileAppAPIResourceController {

    private final JwtService jwtService;
    private final UserService userService;
    private final WikiPostService wikiPostService;
    private final WikiFolderService wikiFolderService;
    private final WikiTagService wikiTagService;

    @GetMapping("/")
    public ResponseEntity<String> getResourceHome(HttpServletRequest request) {
        APIUser apiUser = getUserFromToken(request);
        if (!apiUser.isValid()) {
            return new ResponseEntity<>("", HttpStatus.FORBIDDEN);
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

//        model.addAttribute("tagList", tagList);
//        model.addAttribute("unpublishedList", wikiPostService.findByAuthorAndUnpublished(user.getId()));
//        model.addAttribute("folderTree", wikiFolderService.getFolderTree());
//        model.addAttribute("hideInfo", true);


        return new ResponseEntity<>("", HttpStatus.OK);
    }

    @GetMapping("/homefolders")
    public ResponseEntity<List<GroupedWikiPostList>> getResourceHomeFolders(HttpServletRequest request) {
        APIUser apiUser = getUserFromToken(request);
        if (!apiUser.isValid()) {
            return new ResponseEntity<>(new ArrayList<>(), HttpStatus.FORBIDDEN);
        }

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

        return new ResponseEntity<>(folderPosts, HttpStatus.OK);

    }

    @GetMapping("/{id}")
    public ResponseEntity<WikiPost> getResourceArticle(@PathVariable BigInteger id, HttpServletRequest request) {
        APIUser apiUser = getUserFromToken(request);
        if (!apiUser.isValid()) {
            return new ResponseEntity<>(new WikiPost(), HttpStatus.FORBIDDEN);
        }

        Optional<WikiPost> wikiPost = wikiPostService.findById(id);
        if (wikiPost.isEmpty()) {
            return new ResponseEntity<>(new WikiPost(), HttpStatus.BAD_REQUEST);
        }

        return new ResponseEntity<>(wikiPost.get(), HttpStatus.OK);

    }

    @PostMapping("/article")
    public ResponseEntity<WikiPost> getResourceArticleByName(@ModelAttribute SingleStringModel stringModel, HttpServletRequest request) {
        APIUser apiUser = getUserFromToken(request);
        if (!apiUser.isValid()) {
            return new ResponseEntity<>(new WikiPost(), HttpStatus.FORBIDDEN);
        }

        // split folder and name
        String[] folderList = stringModel.name().split("/");
        String postURLName = folderList[folderList.length-1];
        String postName = postURLName.replace("-", " ");
        String folders = "";
        WikiPost post = new WikiPost();
                post.setPublished(false);

        // build folder
        for ( int i=0; i<folderList.length-1; i++ ) {
            folders = folders + "/" + folderList[i];
        }

        // find posts with name
        List<WikiPost> wikiPostList = wikiPostService.findByTitle(postName);
        if (wikiPostList.isEmpty()) {
            return new ResponseEntity<>(new WikiPost(), HttpStatus.BAD_REQUEST);
        }
        // return only post in correct folder
        for (WikiPost p : wikiPostList) {
            if (p.getFolder().equals(folders)) {
                post = p;
            }
        }

        return new ResponseEntity<>(post, HttpStatus.OK);

    }

    @PostMapping("/folder")
    public ResponseEntity<GroupedWikiPostList> getResourceFolder(@ModelAttribute SingleStringModel stringModel, HttpServletRequest request) {
        APIUser apiUser = getUserFromToken(request);
        if (!apiUser.isValid()) {
            return new ResponseEntity<>(new GroupedWikiPostList(), HttpStatus.FORBIDDEN);
        }

        String folder = "/"+stringModel.name();

        List<WikiFolder> subs = wikiFolderService.findAllByFolderNameStartsWith(folder);
        List<WikiFolder> sub = new ArrayList<>();
        for (WikiFolder f : subs) {
            if (!f.getFolder().equals(folder)) {
                String fSubString = f.getFolder().substring(folder.length());
                String[] subDirs = fSubString.split("/");
                if(subDirs.length <= 2) {
                    sub.add(f);
                }
            }
        }

        List<WikiPost> posts = wikiPostService.findAllByFolder(folder);
        int pinned = 0;
        for (WikiPost post : posts) {
            if (post.isPinToTop()) {
                pinned++;
            }
        }
        GroupedWikiPostList gp =
                new GroupedWikiPostList(
                        folder,
                        posts,
                        posts.size(),
                        sub,
                        pinned);

        return new ResponseEntity<>(gp, HttpStatus.OK);

    }

    private APIUser getUserFromToken(HttpServletRequest request) {
        final String authHeader = request.getHeader("Authorization");
        final String token;
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);
            try {
                Optional<User> currentUser = userService.findByUsername(jwtService.extractUsername(token));
                if (currentUser.isPresent()) {
                    if (jwtService.isTokenValid(token, currentUser.get())) {
                        return new APIUser(true, currentUser.get());
                    }
                }
            } catch (ExpiredJwtException e) {
                System.out.println("Token Expired");
            }
        }
        return new APIUser(false, new User());
    }
}
