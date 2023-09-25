package net.dahliasolutions.controllers.store;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import net.dahliasolutions.models.*;
import net.dahliasolutions.models.department.DepartmentRegional;
import net.dahliasolutions.models.position.Position;
import net.dahliasolutions.models.position.PositionSelectedModel;
import net.dahliasolutions.models.records.BigIntegerStringModel;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
    private final EventService eventService;

    @ModelAttribute
    public void addAttributes(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) auth.getPrincipal();
        List<StoreCategory> categoryList = categoryService.findAll();

        model.addAttribute("moduleTitle", "Store");
        model.addAttribute("moduleLink", "/store");
        model.addAttribute("userId", user.getId());
        model.addAttribute("categoryList", categoryList);
        model.addAttribute("showCategories", true);
    }

    @GetMapping()
    public String goStoreHome(@RequestParam Optional<Integer> page, @RequestParam Optional<Integer> elements, Model model, HttpSession session) {
        AdminSettings adminSettings = adminSettingsService.getAdminSettings();
        if (!adminSettings.getStoreHome().equals("")) {
            redirectService.setHistory(session, "/store");
            model.addAttribute("wikiPost", getStoreHomeFromPath(adminSettings.getStoreHome()));
            return "store/storeHome";
        }
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) auth.getPrincipal();
        DepartmentRegional department = departmentService.findByName(user.getDepartment().getName()).get();

        // create pageable request
        if (page.isEmpty()) {
            page = Optional.of(0);
        }
        if (elements.isEmpty()){
            elements = Optional.of(2);
        }
        Pageable pageRequest = PageRequest.of(page.get(), elements.get());

        Page<StoreItem> itemList;
        if (allowByAdmin()) {
            itemList = storeItemService.findAll(pageRequest);
        } else {
            if (adminSettings.isRestrictStorePosition() && adminSettings.isRestrictStoreDepartment()) {
                //itemList = storeItemService.findAllByAvailableAndPositionListContainsAndDepartment(user.getPosition(), department.getId());
                itemList = storeItemService.findAllByAvailableAndDepartmentAndPositionList(true, department, user.getPosition(), pageRequest);
            } else if (adminSettings.isRestrictStorePosition()) {
                //itemList = storeItemService.findAllByAvailableAndPositionListContains(user.getPosition());
                itemList = storeItemService.findAllByAvailableAndPositionList(true, user.getPosition(), pageRequest);
            } else if (adminSettings.isRestrictStoreDepartment()) {
                itemList = storeItemService.findAllByAvailableAndDepartment(true, department, pageRequest);
            } else {
                itemList = storeItemService.findAllByAvailable(true, pageRequest);
            }
        }

        model.addAttribute("storeItems", itemList);
        redirectService.setHistory(session, "/store");
        return "store/index";
    }

    @GetMapping("/{category}/{subCategory}")
    public String goStoreCategory(@RequestParam Optional<Integer> page, @RequestParam Optional<Integer> elements, @PathVariable String category, @PathVariable Optional<String> subCategory, Model model, HttpSession session) {
        AdminSettings adminSettings = adminSettingsService.getAdminSettings();

        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        DepartmentRegional department = departmentService.findByName(user.getDepartment().getName()).get();

        String subName = "";
        if (subCategory.isPresent()) {
            subName = subCategory.get();
        }
        Optional<StoreCategory> storeCategory = categoryService.findByName(category);
        Optional<StoreSubCategory> storeSubCategory = subCategoryService.findByNameAndCategoryId(subName, storeCategory.get().getId());

        // create pageable request
        if (page.isEmpty()) {
            page = Optional.of(0);
        }
        if (elements.isEmpty()){
            elements = Optional.of(2);
        }
        Pageable pageRequest = PageRequest.of(page.get(), elements.get());

        Page<StoreItem> itemList;
        if (allowByAdmin()) {
            if (storeSubCategory.isPresent()) {
                itemList = storeItemService.findAllByAvailableAndSubCategory(true, storeSubCategory.get(), pageRequest);
            } else {
                itemList = storeItemService.findAllByAvailableAndCategory(true, storeCategory.get(), pageRequest);
            }
        } else {
            if (adminSettings.isRestrictStorePosition() && adminSettings.isRestrictStoreDepartment()) {
                if (storeSubCategory.isPresent()) {
                    itemList = storeItemService.findAllByAvailableAndSubCategoryAndDepartmentAndPositionList(true, storeSubCategory.get(), department, user.getPosition(), pageRequest);
                } else {
                    itemList = storeItemService.findAllByAvailableAndCategoryAndDepartmentAndPositionList(true, storeCategory.get(), department, user.getPosition(), pageRequest);
                }
            } else if (adminSettings.isRestrictStorePosition()) {
                if (storeSubCategory.isPresent()) {
                    itemList = storeItemService.findAllByAvailableAndSubCategoryAndPositionList(true, storeSubCategory.get(), user.getPosition(), pageRequest);
                } else {
                    itemList = storeItemService.findAllByAvailableAndCategoryAndPositionList(true, storeCategory.get(), user.getPosition(), pageRequest);
                }
            } else if (adminSettings.isRestrictStoreDepartment()) {
                if (storeSubCategory.isPresent()) {
                    itemList = storeItemService.findAllByAvailableAndSubCategoryAndDepartment(true, storeSubCategory.get(), department, pageRequest);
                } else {
                    itemList = storeItemService.findAllByAvailableAndCategoryAndDepartment(true, storeCategory.get(), department, pageRequest);
                }
            } else {
                if (storeSubCategory.isPresent()) {
                    itemList = storeItemService.findAllByAvailableAndSubCategory(true, storeSubCategory.get(), pageRequest);
                } else {
                    itemList = storeItemService.findAllByAvailableAndCategory(true, storeCategory.get(), pageRequest);
                }
            }
        }


        model.addAttribute("storeItems", itemList);
        model.addAttribute("selectedCategory", category);
        model.addAttribute("selectedSubCategory", subName);
        redirectService.setHistory(session, "/store/"+category+"/"+subName);
        return "store/index";
    }

    @GetMapping("/new")
    public String getNewItem(Model model, HttpSession session) {
        if (!allowByEditor()) {
            redirectService.pathName(session, "/store");
        }

        StoreItem storeItem = new StoreItem();
        List<User> userList = userService.findAll();
        List<DepartmentRegional> departmentRegionalList = departmentService.findAll();
        List<Position> positionList = positionService.findAll();

        model.addAttribute("storeItem", storeItem);
        model.addAttribute("userList", userList);
        model.addAttribute("positionList", positionList);
        model.addAttribute("departmentList", departmentRegionalList);
        model.addAttribute("showCategories", false);
        return "store/itemNew";
    }

    @PostMapping("/create")
    public String createStoreItem(@ModelAttribute StoreItemModel storeItemModel, Model model, HttpSession session) {
        if (!allowByEditor()) {
            redirectService.pathName(session, "/store");
        }

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

        // send any additional notifications
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        AppEvent notifyEvent = eventService.createEvent(new AppEvent(
                null,
                user.getFullName()+" Created a new store item.",
                newItem.getName()+" was added to the store by "+user.getFullName(),
                newItem.getId().toString(),
                EventModule.Store,
                EventType.New,
                new ArrayList<>()
        ));

        return "redirect:/store/item/"+newItem.getId().toString();
    }

    @GetMapping("/item/{id}")
    public String getItem(@PathVariable("id") BigInteger id, Model model, HttpSession session) {
        redirectService.setHistory(session, "/store/item/"+id);
        Optional<StoreItem> storeItem = storeItemService.findById(id);

        if (storeItem.isPresent()) {
            model.addAttribute("storeItem", storeItem.get());
        } else {
            model.addAttribute("storeItem", new StoreItem());
        }

        return "store/item";
    }

    @GetMapping("/edit/{id}")
    public String getItemToEdit(@PathVariable BigInteger id, Model model, HttpSession session) {
        if (!allowByEditor()) {
            redirectService.pathName(session, "/store");
        }

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
        model.addAttribute("showCategories", false);
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

        // send any additional notifications
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        AppEvent notifyEvent = eventService.createEvent(new AppEvent(
                null,
                user.getFullName()+"Updated a store item.",
                storeItem.get().getName()+" was updated by "+user.getFullName(),
                storeItem.get().getId().toString(),
                EventModule.Store,
                EventType.Updated,
                new ArrayList<>()
        ));

        return "redirect:/store/item/"+storeItemModel.id().toString();
    }

    @GetMapping("/settings")
    public String getStoreSettings(Model model) {
        // Categories
        List<StoreCategory> categoryList = categoryService.findAll();
        // Request Target
        StoreSetting storeSetting = storeSettingService.getStoreSetting();
        BigInteger userId = BigInteger.valueOf(0);
        if (storeSetting.getUser() != null) {
            userId = storeSetting.getUser().getId();
        }
        List<RequestNotifyTarget> targetList = Arrays.asList(RequestNotifyTarget.values());
        List<User> userList = userService.findAllByRoles("ADMIN_WRITE,RESOURCE_WRITE,RESOURCE_SUPERVISOR");
        // Notifications
        List<Notification> notificationList = notificationService.findAllByModule(EventModule.Store);
        List<User> users = userService.findAll();
        List<BigIntegerStringModel> notificationUsers = new ArrayList<>();
        for (User u : users) {
            notificationUsers.add(new BigIntegerStringModel(u.getId(), u.getFirstName()+' '+u.getLastName()));
        }

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
        model.addAttribute("typeList", Arrays.asList(EventType.values()));
        model.addAttribute("notificationUsers", notificationUsers);
        model.addAttribute("showCategories", false);

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
        model.addAttribute("showCategories", false);
        return "store/imageManager";
    }

    @GetMapping("/search/{searchTerm}")
    public String searchArticle(@PathVariable String searchTerm, Model model, HttpSession session) {
        String searcher = URLDecoder.decode(searchTerm, StandardCharsets.UTF_8);

        AdminSettings adminSettings = adminSettingsService.getAdminSettings();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) auth.getPrincipal();
        DepartmentRegional department = departmentService.findByName(user.getDepartment().getName()).get();

        List<StoreItem> searchList = storeItemService.searchAll(searcher);
        List<StoreItem> itemList = storeItemService.searchAll(searcher);
        if (allowByAdmin()) {
            itemList = searchList;
        } else {
            if (adminSettings.isRestrictStorePosition() && adminSettings.isRestrictStoreDepartment()) {
                for (StoreItem item : searchList) {
                    if (item.getPositionList().contains(user.getPosition()) && item.getDepartment().equals(department)) {
                        itemList.add(item);
                    }
                }
            } else if (adminSettings.isRestrictStorePosition()) {
                for (StoreItem item : searchList) {
                    if (item.getPositionList().contains(user.getPosition())) {
                        itemList.add(item);
                    }
                }
            } else if (adminSettings.isRestrictStoreDepartment()) {
                for (StoreItem item : searchList) {
                    if (item.getDepartment().equals(department)) {
                        itemList.add(item);
                    }
                }
            } else {
                for (StoreItem item : searchList) {
                    itemList = searchList;
                }
            }
        }

        model.addAttribute("storeItems", itemList);
        model.addAttribute("searchTerm", searcher);
        redirectService.setHistory(session, "/store/search/title/"+searchTerm);
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
        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Collection<UserRoles> roles = currentUser.getUserRoles();

        for (UserRoles role : roles){
            if (role.getName().equals("ADMIN_WRITE") || role.getName().equals("STORE_SUPERVISOR")) {
                return true;
            }
        }
        return false;
    }
    private boolean allowByEditor(){
        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Collection<UserRoles> roles = currentUser.getUserRoles();

        for (UserRoles role : roles){
            if (role.getName().equals("ADMIN_WRITE") || role.getName().equals("STORE_WRITE")
                    || role.getName().equals("STORE_SUPERVISOR")) {
                return true;
            }
        }
        return false;
    }

}
