package net.dahliasolutions.controllers.app;

import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import net.dahliasolutions.models.APIUser;
import net.dahliasolutions.models.AppItem;
import net.dahliasolutions.models.AppServer;
import net.dahliasolutions.models.campus.Campus;
import net.dahliasolutions.models.department.DepartmentRegional;
import net.dahliasolutions.models.records.*;
import net.dahliasolutions.models.support.*;
import net.dahliasolutions.models.user.User;
import net.dahliasolutions.models.user.UserRoles;
import net.dahliasolutions.models.wiki.*;
import net.dahliasolutions.services.AdminSettingsService;
import net.dahliasolutions.services.JwtService;
import net.dahliasolutions.services.RedirectService;
import net.dahliasolutions.services.campus.CampusService;
import net.dahliasolutions.services.department.DepartmentCampusService;
import net.dahliasolutions.services.department.DepartmentRegionalService;
import net.dahliasolutions.services.support.*;
import net.dahliasolutions.services.user.UserRolesService;
import net.dahliasolutions.services.user.UserService;
import net.dahliasolutions.services.wiki.WikiFolderService;
import net.dahliasolutions.services.wiki.WikiPostService;
import net.dahliasolutions.services.wiki.WikiTagService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
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
    private final AppServer appServer;

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
        }// find parent folders
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
                    sub.add(f);
                }
            }

            List<WikiPost> posts = wikiPostService.findAllByFolder(p);
            GroupedWikiPostList gp =
                    new GroupedWikiPostList(
                            p.replace("/", ""),
                            posts,
                            posts.size(),
                            sub);
            folderPosts.add(gp);
        }

        return new ResponseEntity<>(folderPosts, HttpStatus.OK);

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
