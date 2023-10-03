package net.dahliasolutions.controllers.wiki;

import lombok.RequiredArgsConstructor;
import net.dahliasolutions.models.position.Position;
import net.dahliasolutions.models.position.PositionSelectedModel;
import net.dahliasolutions.models.records.*;
import net.dahliasolutions.models.user.User;
import net.dahliasolutions.models.wiki.*;
import net.dahliasolutions.services.AdminSettingsService;
import net.dahliasolutions.services.position.PositionService;
import net.dahliasolutions.services.wiki.WikiFolderService;
import net.dahliasolutions.services.wiki.WikiNavigatorService;
import net.dahliasolutions.services.wiki.WikiPostService;
import net.dahliasolutions.services.wiki.WikiTagService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/wiki")
public class WikiAPIController {

    private final WikiPostService wikiPostService;
    private final WikiFolderService wikiFolderService;
    private final WikiTagService wikiTagService;
    private final WikiNavigatorService navigatorService;
    private final PositionService positionService;
    private final AdminSettingsService adminSettingsService;

    @PostMapping("/save")
    public BigInteger postWiki(@ModelAttribute WikiPostModel wikiPostModel) {
        Optional<WikiPost> wikiPost = wikiPostService.findById(wikiPostModel.id());
        boolean doc = Boolean.valueOf(wikiPostModel.anonymous());
        boolean pub = Boolean.valueOf(wikiPostModel.published());
        boolean info = Boolean.valueOf(wikiPostModel.hideInfo());
        boolean pin = Boolean.valueOf(wikiPostModel.pinToTop());

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
                newPost.setAnonymous(doc);
                newPost.setPublished(pub);
                newPost.setHideInfo(info);
                newPost.setPinToTop(pin);
                newPost.setTagList(new ArrayList<>());
                newPost.setPositionList(new ArrayList<>());
            return wikiPostService.createWikiPost(newPost).getId();
        }

        wikiPost.get().setTitle(wikiPostModel.title());
        wikiPost.get().setBody(wikiPostModel.body());
        wikiPost.get().setFolder(wikiPostModel.folder());
        wikiPost.get().setLastUpdated(LocalDateTime.now());
        wikiPost.get().setSummary(summary);
        wikiPost.get().setAnonymous(doc);
        wikiPost.get().setPublished(pub);
        wikiPost.get().setHideInfo(info);
        wikiPost.get().setPinToTop(pin);

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

        wikiTagModel.setReferencedTag(0);
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
            mergedTagCount.setReferencedTag(mrgCount);
            return mergedTagCount;
        }

        if (updateTag.isPresent()) {
            updateTag.get().setName(wikiTagModel.getName());
            wikiTagService.save(updateTag.get());
            return wikiTagModel;
        }
        return null;
    }

    @PostMapping("/foldermanager/new")
    public WikiFolder newFolderByManager(@ModelAttribute SingleStringModel folderModel) {
        // confirm starting slash
        String folderName = folderModel.name();
        if (!folderModel.name().startsWith("/")) {
            folderName = "/"+folderModel.name();
        }

        String[] folders = folderName.split("/");
        String baseFolder = "";
        WikiFolder latestFolder = new WikiFolder();
        for (int i=1; i < folders.length; i++) {
            baseFolder = baseFolder+"/"+folders[i];
            Optional<WikiFolder> wikiFolder = wikiFolderService.findByFolder(baseFolder);
            if (wikiFolder.isEmpty()) {
                wikiFolderService.save(baseFolder);
            }
        }

        return latestFolder;
    }

    @PostMapping("/foldermanager/delete")
    public WikiFolder deleteFolderByManager(@ModelAttribute SingleStringModel folderModel) {
        Optional<WikiFolder> wikiFolder = wikiFolderService.findByFolder(folderModel.name());
        if (wikiFolder.isEmpty()) { return null; }

        wikiPostService.removeFolder(wikiFolder.get());
        wikiFolderService.deleteByFolder(wikiFolder.get());

        return wikiFolder.get();
    }

    @PostMapping("/foldermanager/update")
    public DoubleStringModel updateFolderByManager(@ModelAttribute DoubleStringModel folderModel) {
        Optional<WikiFolder> srcFolder = wikiFolderService.findByFolder(folderModel.nameSource());
        Optional<WikiFolder> destFolder = wikiFolderService.findByFolder(folderModel.nameDestination());

        if (destFolder.isPresent()) {
            return folderModel;
        }

        wikiPostService.updateFolder(folderModel.nameSource(), folderModel.nameDestination());
        wikiFolderService.deleteByFolder(srcFolder.get());

        wikiFolderService.save(folderModel.nameDestination());
        DoubleStringModel updatedFolder = new DoubleStringModel(
                folderModel.nameDestination(), folderModel.nameDestination());

        return updatedFolder;
    }

    @PostMapping("/updatewikihome")
    public SingleStringModel updateWikiHome(@ModelAttribute SingleStringModel wikiHomeModel) {
        adminSettingsService.setWikiHome(wikiHomeModel.name());
        return wikiHomeModel;
    }

    @GetMapping("/foldertree")
    public WikiFolderTree getFoldersByTree() {
        return wikiFolderService.getFolderTree();
    }

    @PostMapping("/updatedocshome")
    public SingleStringModel updateDocsHome(@ModelAttribute SingleStringModel wikiHomeModel) {
        adminSettingsService.setDocumentationHome(wikiHomeModel.name());
        return wikiHomeModel;
    }

    @PostMapping("/updateportalhome")
    public SingleStringModel updatePortalHome(@ModelAttribute SingleStringModel portalHomeModel) {
        adminSettingsService.setPortalHome(portalHomeModel.name());
        return portalHomeModel;
    }

    @PostMapping("/updatesitehome")
    public SingleStringModel updateStoreHome(@ModelAttribute SingleStringModel portalHomeModel) {
        adminSettingsService.setStoreHome(portalHomeModel.name());
        return portalHomeModel;
    }

    @GetMapping("/navigator/list")
    public List<WikiNavigator> listNavigatorItems() {
        List<WikiNavigator> allItems = navigatorService.findAll();
        return allItems;
    }

    @PostMapping("/navigator/new")
    public WikiNavigator newNavigatorItem(@ModelAttribute WikiNavigator navModel) {
        WikiNavigator nav = navigatorService.createItem(navModel);
        return nav;
    }

    @GetMapping("/navigator/get/{id}")
    public WikiNavigator updateNavigatorItem(@PathVariable Integer id) {
        Optional<WikiNavigator> navItem = navigatorService.findById(id);
        if (navItem.isPresent()) {
            return navItem.get();
        }
        return new WikiNavigator();
    }

    @PostMapping("/navigator/delete")
    public SingleIntegerModel deleteNavigatorItem(@ModelAttribute SingleIntegerModel navModel) {
        navigatorService.deleteById(navModel.id());
        return navModel;
    }

    @PostMapping("/navigator/update")
    public WikiNavigator updateNavigatorItem(@ModelAttribute WikiNavigator navModel) {
        return navigatorService.save(navModel);
    }

    @GetMapping("/getpermissions/{id}")
    public List<PositionSelectedModel> updateNavigatorItem(@PathVariable BigInteger id) {
        List<Position> positionList = positionService.findAll();
        List<PositionSelectedModel> positions = new ArrayList<>();

        Optional<WikiPost> post = wikiPostService.findById(id);
        if (post.isPresent()) {
            for (Position pos : positionList) {
                PositionSelectedModel posSelect = new PositionSelectedModel(
                        pos.getId(),
                        pos.getLevel(),
                        pos.getName(),
                        false
                );
                if (post.get().getPositionList().contains(pos)) {
                    posSelect.setSelected(true);
                }
                positions.add(posSelect);
            }
        }
        return positions;
    }

    @PostMapping("/setpermissions")
    public SingleBigIntegerModel updatePostPermissions(@ModelAttribute BigIntegerStringModel postModel) {
        Optional<WikiPost> post = wikiPostService.findById(postModel.id());
        if (post.isPresent()) {
            List<String> items = Arrays.asList(postModel.name().split("\s"));
            ArrayList<Position> pl = new ArrayList<>();
            for (String s : items) {
                if (!s.equals("")) {
                    try {
                        int i = Integer.parseInt(s);
                        Optional<Position> p = positionService.findById(BigInteger.valueOf(i));
                        if (p.isPresent()) {
                            pl.add(p.get());
                        }
                    } catch (Error e) {
                        System.out.println(e);
                    }
                }
            }

            post.get().setPositionList(pl);
            wikiPostService.save(post.get());
        }
        return new SingleBigIntegerModel(postModel.id());
    }
}
