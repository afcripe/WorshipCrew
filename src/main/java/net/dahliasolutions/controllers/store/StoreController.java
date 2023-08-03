package net.dahliasolutions.controllers.store;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import net.dahliasolutions.models.AdminSettings;
import net.dahliasolutions.models.department.DepartmentRegional;
import net.dahliasolutions.models.position.Position;
import net.dahliasolutions.models.position.PositionSelectedModel;
import net.dahliasolutions.models.store.*;
import net.dahliasolutions.models.user.User;
import net.dahliasolutions.models.wiki.WikiPost;
import net.dahliasolutions.models.wiki.WikiTag;
import net.dahliasolutions.models.wiki.WikiTagReference;
import net.dahliasolutions.services.*;
import net.dahliasolutions.services.department.DepartmentRegionalService;
import net.dahliasolutions.services.position.PositionService;
import net.dahliasolutions.services.store.StoreCategoryService;
import net.dahliasolutions.services.store.StoreImageService;
import net.dahliasolutions.services.store.StoreItemService;
import net.dahliasolutions.services.store.StoreSubCategoryService;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

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

        List<StoreItem> itemList = storeItemService.findAll();

        model.addAttribute("storeItems", itemList);
        return "store/index";
    }


    @GetMapping("/{category}")
    public String goStoreCategory(@PathVariable String category, Model model, HttpSession session) {
        redirectService.setHistory(session, "/store");
        String selectCategory = "";
        String selectSubCategory = "";

        Optional<StoreSubCategory> subCategory = subCategoryService.findByName(category);

        List<StoreItem> itemList = new ArrayList<>();
        if (subCategory.isPresent()) {
            itemList = storeItemService.findBySubCategory(subCategory.get());
            selectSubCategory = subCategory.get().getName();
            selectCategory = subCategory.get().getStoreCategory().getName();
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

    @GetMapping("/item/edit/{id}")
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
        List<StoreCategory> categoryList = categoryService.findAll();
        model.addAttribute("categoryList", categoryList);
        model.addAttribute("storeHome", adminSettingsService.getAdminSettings().getStoreHome());
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

}
