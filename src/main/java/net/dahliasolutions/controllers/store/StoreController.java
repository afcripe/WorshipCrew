package net.dahliasolutions.controllers.store;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import net.dahliasolutions.models.AdminSettings;
import net.dahliasolutions.models.Notification;
import net.dahliasolutions.models.EventModule;
import net.dahliasolutions.models.department.DepartmentRegional;
import net.dahliasolutions.models.position.Position;
import net.dahliasolutions.models.position.PositionSelectedModel;
import net.dahliasolutions.models.store.*;
import net.dahliasolutions.models.user.User;
import net.dahliasolutions.models.user.UserRoles;
import net.dahliasolutions.models.wiki.WikiPost;
import net.dahliasolutions.services.*;
import net.dahliasolutions.services.department.DepartmentRegionalService;
import net.dahliasolutions.services.position.PositionService;
import net.dahliasolutions.services.store.*;
import net.dahliasolutions.services.user.UserService;
import net.dahliasolutions.services.wiki.WikiPostService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigInteger;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Controller
@RequestMapping("/store")
@RequiredArgsConstructor
public class StoreController {

    private final StoreItemService storeItemService;
    private final PositionService positionService;
    private final DepartmentRegionalService departmentService;
    private final UserService userService;
    private final StoreImageService storedImageService;
    private final RedirectService redirectService;
    private final StoreCategoryService categoryService;
    private final StoreSubCategoryService subCategoryService;
    private final AdminSettingsService adminSettingsService;
    private final WikiPostService wikiPostService;
    private final NotificationService notificationService;
    private final StoreSettingService storeSettingService;

    @ModelAttribute
    public void addAttributes(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) auth.getPrincipal();
        List<StoreCategory> categoryList = categoryService.findAll();

        model.addAttribute("moduleTitle", "Store");
        model.addAttribute("moduleLink", "/store");
        model.addAttribute("userId", user.getId());
        model.addAttribute("categoryList", categoryList);
    }

    @GetMapping("")
    public String goStoreHome(Model model, HttpSession session) {
        redirectService.setHistory(session, "/store");
        AdminSettings adminSettings = adminSettingsService.getAdminSettings();
        if (!adminSettings.getStoreHome().equals("")) {
            model.addAttribute("wikiPost", getStoreHomeFromPath(adminSettings.getStoreHome()));
            return "store/storeHome";
        }
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) auth.getPrincipal();
        DepartmentRegional department = departmentService.findByName(user.getDepartment().getName()).get();

        List<StoreItem> itemList;
        if (allowByAdmin()) {
            itemList = storeItemService.findAll();
        } else {
            if (adminSettings.isRestrictStorePosition() && adminSettings.isRestrictStoreDepartment()) {
                itemList = storeItemService.findAllByAvailableAndPositionListContainsAndDepartment(user.getPosition(), department.getId());
            } else if (adminSettings.isRestrictStorePosition()) {
                itemList = storeItemService.findAllByAvailableAndPositionListContains(user.getPosition());
            } else if (adminSettings.isRestrictStoreDepartment()) {
                itemList = storeItemService.findAllByAvailableAndDepartment(department.getId());
            } else {
                itemList = storeItemService.findAllByAvailable();
            }
        }

        model.addAttribute("storeItems", itemList);
        return "store/index";
    }


    @GetMapping("/{category}/{subCategory}")
    public String goStoreCategory(@PathVariable String category, @PathVariable String subCategory, Model model, HttpSession session) {
        redirectService.setHistory(session, "/store/"+category+"/"+subCategory);
        String selectCategory = "";
        String selectSubCategory = "";

        Optional<StoreCategory> storeCategory = categoryService.findByName(category);
        Optional<StoreSubCategory> storeSubCategory = subCategoryService.findByNameAndCategoryId(subCategory, storeCategory.get().getId());

        List<StoreItem> itemList = new ArrayList<>();
        if (storeSubCategory.isPresent()) {
            itemList = storeItemService.findBySubCategory(storeSubCategory.get());
            selectSubCategory = storeSubCategory.get().getName();
            selectCategory = storeCategory.get().getName();
        }

        model.addAttribute("storeItems", itemList);
        model.addAttribute("selectedCategory", selectCategory);
        model.addAttribute("selectedSubCategory", selectSubCategory);
        return "store/index";
    }

    @GetMapping("/new")
    public String getNewItem(Model model) {

        StoreItem storeItem = new StoreItem();
        List<User> userList = userService.findAll();
        List<DepartmentRegional> departmentRegionalList = departmentService.findAll();
        List<Position> positionList = positionService.findAll();

        model.addAttribute("storeItem", storeItem);
        model.addAttribute("userList", userList);
        model.addAttribute("positionList", positionList);
        model.addAttribute("departmentList", departmentRegionalList);
        return "store/itemNew";
    }

    @PostMapping("/create")
    public String createStoreItem(@ModelAttribute StoreItemModel storeItemModel, Model model, HttpSession session) {
        if (storeItemModel.name().equals("")) {
            List<User> userList = userService.findAll();
            List<DepartmentRegional> departmentRegionalList = departmentService.findAll();
            List<Position> positionList = positionService.findAll();

            model.addAttribute("storeItem", storeItemModel);
            model.addAttribute("userList", userList);
            model.addAttribute("positionList", positionList);
            model.addAttribute("departmentList", departmentRegionalList);
            session.setAttribute("msgError", "Product Name is Required.");
            return "store/itemNew";
        }

        boolean specialOrder = storeItemModel.specialOrder() != null;
        boolean available = storeItemModel.available() != null;

        StoreItem storeItem = new StoreItem();
        storeItem.setName(storeItemModel.name());
        storeItem.setDescription(storeItemModel.description());
        storeItem.setSpecialOrder(specialOrder);
        storeItem.setAvailable(available);
        storeItem.setLeadTime(storeItemModel.leadTime());
        if (storeItemModel.department() != null) {
            storeItem.setDepartment(departmentService.findById(storeItemModel.department()).orElse(null));
        }
        if (storeItemModel.category() != null) {
            storeItem.setCategory(categoryService.findById(storeItemModel.category()).orElse(null));
        }
        if (storeItemModel.subCategory() != null) {
            storeItem.setSubCategory(subCategoryService.findById(storeItemModel.subCategory()).orElse(null));
        }
        if (storeItemModel.image() != null) {
            storeItem.setImage(storedImageService.findById(storeItemModel.image()).orElse(null));
        }

        List<String> items = Arrays.asList(storeItemModel.position().split("\s"));
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

        storeItem.setPositionList(pl);

        StoreItem newItem = storeItemService.createStoreItem(storeItem);
        session.setAttribute("msgSuccess", "Item successfully added.");

        return "redirect:/store/item/"+newItem.getId().toString();
    }

    @GetMapping("/item/{id}")
    public String getItem(@PathVariable("id") BigInteger id, Model model, HttpSession session) {
        redirectService.setHistory(session, "/store/item/"+id);
        Optional<StoreItem> storeItem = storeItemService.findById(id);
        if (storeItem.isEmpty()) {
            session.setAttribute("msgError", "Item not found.");
        }
        model.addAttribute("storeItem", storeItem.get());
        return "store/item";
    }

    @GetMapping("/edit/{id}")
    public String getItemToEdit(@PathVariable BigInteger id, Model model, HttpSession session) {
        BigInteger catId = BigInteger.valueOf(0);
        BigInteger subCatId = BigInteger.valueOf(0);

        Optional<StoreItem> storeItem = storeItemService.findById(id);
        if (storeItem.isEmpty()) {
            session.setAttribute("msgError", "Item not found.");
            return redirectService.pathName(session, "/store");
        }

        List<DepartmentRegional> departmentRegionalList = departmentService.findAll();
        List<StoreCategory> categoryList = categoryService.findAll();

        List<Position> allPositionList = positionService.findAll();
        List<Object> positionList = new ArrayList<>();
        for (Position p : allPositionList) {
            PositionSelectedModel selected = new PositionSelectedModel();
                selected.setId(p.getId());
                selected.setName(p.getName());
                selected.setSelected(false);
                for (Position sp : storeItem.get().getPositionList()) {
                    if (sp.getId().equals(p.getId())) {
                        selected.setSelected(true);
                    }
                }
            positionList.add(selected);
        }

        if (storeItem.get().getCategory() != null) {
            try {
                catId = storeItem.get().getCategory().getId();
            } catch (Error e) {
                System.out.println(e);
            }
        }
        if (storeItem.get().getSubCategory() != null) {
            try {
                subCatId = storeItem.get().getSubCategory().getId();
            } catch (Error e) {
                System.out.println(e);
            }
        }

        model.addAttribute("storeItem", storeItem.get());
        model.addAttribute("positionList", positionList);
        model.addAttribute("departmentList", departmentRegionalList);
        model.addAttribute("categoryList", categoryList);
        model.addAttribute("categoryId", catId);
        model.addAttribute("subCategoryId", subCatId);
        return "store/itemEdit";
    }

    @PostMapping("/update")
    public String updateStoreItem(@ModelAttribute StoreItemModel storeItemModel, Model model, HttpSession session) {
        Optional<StoreItem> storeItem = storeItemService.findById(storeItemModel.id());
        if (storeItem.isEmpty()) {
            session.setAttribute("msgError", "Item not found..");
            return redirectService.pathName(session, "/store");
        }

        boolean specialOrder = storeItemModel.specialOrder() != null;
        boolean available = storeItemModel.available() != null;

        String desc = storeItemModel.description();
        System.out.println(desc);

        storeItem.get().setName(storeItemModel.name());
        storeItem.get().setDescription(storeItemModel.description());
        storeItem.get().setSpecialOrder(specialOrder);
        storeItem.get().setAvailable(available);
        storeItem.get().setLeadTime(storeItemModel.leadTime());
        if (storeItemModel.department() != null) {
            storeItem.get().setDepartment(departmentService.findById(storeItemModel.department()).orElse(null));
        }
        if (storeItemModel.category() != null) {
            storeItem.get().setCategory(categoryService.findById(storeItemModel.category()).orElse(null));
        }
        if (storeItemModel.subCategory() != null) {
            storeItem.get().setSubCategory(subCategoryService.findById(storeItemModel.subCategory()).orElse(null));
        }
        if (storeItemModel.image() != null) {
            storeItem.get().setImage(storedImageService.findById(storeItemModel.image()).orElse(null));
        }

        List<String> items = Arrays.asList(storeItemModel.position().split("\s"));
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

        storeItem.get().setPositionList(pl);

        storeItemService.save(storeItem.get());
        session.setAttribute("msgSuccess", "Item successfully updated.");

        return "redirect:/store/item/"+storeItemModel.id().toString();
    }

    @GetMapping("/settings")
    public String getStoreSettings(Model model) {
        List<Notification> notificationList = notificationService.findAllByModule(EventModule.Store);
        List<StoreCategory> categoryList = categoryService.findAll();

        StoreSetting storeSetting = storeSettingService.getStoreSetting();
        BigInteger userId = BigInteger.valueOf(0);
        if (storeSetting.getUser() != null) {
            userId = storeSetting.getUser().getId();
        }

        List<StoreNotifyTarget> targetList = Arrays.asList(StoreNotifyTarget.values());
        List<User> userList = userService.findAllByRoles("ADMIN_WRITE,RESOURCE_WRITE,RESOURCE_SUPERVISOR");

        model.addAttribute("categoryList", categoryList);
        model.addAttribute("storeHome", adminSettingsService.getAdminSettings().getStoreHome());
        model.addAttribute("restrictPosition", adminSettingsService.getAdminSettings().isRestrictStorePosition());
        model.addAttribute("restrictDepartment", adminSettingsService.getAdminSettings().isRestrictStoreDepartment());

        model.addAttribute("storeSetting", storeSetting);
        model.addAttribute("notifyTarget", storeSetting.getNotifyTarget().toString());
        model.addAttribute("userId", userId);
        model.addAttribute("targetList", targetList);
        model.addAttribute("userList", userList);

        model.addAttribute("notificationList", notificationList);
        return "store/settings";
    }

    @GetMapping("/imageManager")
    public String getImageManager(Model model, HttpSession session) {
        redirectService.setHistory(session, "/store/settings");
        List<StoreImage> storeImageList = storedImageService.findAll();
        List<StoreImageModel> imageList = new ArrayList<>();
        for (StoreImage image : storeImageList) {
            StoreImageModel imageModel = new StoreImageModel(image.getId(), image.getName(), image.getDescription(),
                    image.getFileLocation(), 0);
            imageList.add(imageModel);
        }

        List<StoreItem> storeItemList = storeItemService.findAll();
        for (StoreItem storeItem : storeItemList) {
            for (StoreImageModel img : imageList) {
                if (storeItem.getImage().getId().equals(img.getId())) {
                    img.setReferences(img.getReferences()+1);
                }
            }
        }

        model.addAttribute("imageList", imageList);
        return "store/imageManager";
    }

    @GetMapping("/search/{searchTerm}")
    public String searchArticle(@PathVariable String searchTerm, Model model, HttpSession session) {
        redirectService.setHistory(session, "/store/search/title/"+searchTerm);
        String searcher = URLDecoder.decode(searchTerm, StandardCharsets.UTF_8);
        List<StoreItem> itemList = storeItemService.searchAll(searcher);

        model.addAttribute("storeItems", itemList);
        model.addAttribute("searchTerm", searcher);
        return "store/index";
    }

    private WikiPost getStoreHomeFromPath(String path) {
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

    /*  Determine Edit Permissions */
    private boolean allowByAdmin(){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) auth.getPrincipal();
        Collection<UserRoles> roles = user.getUserRoles();

        for (UserRoles role : roles){
            if (role.getName().equals("ADMIN_WRITE")) {
                return true;
            }
        }
        return false;
    }

}
