package net.dahliasolutions.controllers.store;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import net.dahliasolutions.models.AdminSettings;
import net.dahliasolutions.models.AppEvent;
import net.dahliasolutions.models.EventModule;
import net.dahliasolutions.models.EventType;
import net.dahliasolutions.models.store.RequestNotifyTarget;
import net.dahliasolutions.models.records.SingleBigIntegerModel;
import net.dahliasolutions.models.records.SingleStringModel;
import net.dahliasolutions.models.store.*;
import net.dahliasolutions.models.user.Profile;
import net.dahliasolutions.models.user.User;
import net.dahliasolutions.models.user.UserRoles;
import net.dahliasolutions.services.AdminSettingsService;
import net.dahliasolutions.services.EventService;
import net.dahliasolutions.services.store.*;
import net.dahliasolutions.services.user.ProfileService;
import net.dahliasolutions.services.user.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/store")
@RequiredArgsConstructor
public class StoreAPIController {

    private final StoreItemService storeItemService;
    private final ProfileService profileService;
    private final StoreCategoryService categoryService;
    private final StoreSubCategoryService subCategoryService;
    private final StoreItemOptionService optionService;
    private final AdminSettingsService adminSettingsService;
    private final UserService userService;
    private final StoreSettingService storeSettingService;
    private final StoreImageService storeImageService;
    private final EventService eventService;

    @GetMapping("")
    public List<StoreItem> goStoreHome() {
        return storeItemService.findAll();
    }

    @PostMapping("/new")
    public SingleBigIntegerModel postNewStoreItem(@ModelAttribute SingleStringModel item) {
        StoreItem storeItem = new StoreItem();
        storeItem.setName(item.name());
        storeItem = storeItemService.createStoreItem(storeItem);

        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        AppEvent notifyEvent = eventService.createEvent(new AppEvent(
                null,
                currentUser.getFullName()+" Created a new store item.",
                item.name()+" was added to the store by "+currentUser.getFullName(),
                storeItem.getId().toString(),
                EventModule.Store,
                EventType.New,
                new ArrayList<>()
        ));
        return new SingleBigIntegerModel(storeItem.getId());
    }

    @GetMapping ("/display/{style}")
    public void toggleListGrid(@PathVariable String style, HttpSession session) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) auth.getPrincipal();
        Optional<Profile> profile = profileService.findByUser(user);
            profile.get().setStoreLayout(style);
        profileService.save(profile.get());

        if (style.equals("grid")) {
            session.setAttribute("storeLayout", "grid");
        } else {
            session.setAttribute("storeLayout", "list");
        }
    }

    @PostMapping("/setrestriction")
    public String setUserRole(@ModelAttribute SingleStringModel restriction) {
        // check permissions
        if (allowByAdmin()) {
            AdminSettings adminSettings = adminSettingsService.getAdminSettings();
            switch (restriction.name()) {
                case "restrictStorePosition":
                    adminSettingsService.setRestrictStorePosition(!adminSettings.isRestrictStorePosition());
                    break;
                case "restrictStoreDepartment":
                    adminSettingsService.setRestrictStoreDepartment(!adminSettings.isRestrictStoreDepartment());
                    break;
            }
            return "true";
        }


        return "false";
    }

    @PostMapping("/category/new")
    public StoreCategory postNewCategory(@ModelAttribute SingleStringModel item) {
        Optional<StoreCategory> category = categoryService.findByName(item.name());
        if (category.isEmpty()) {
            return categoryService.createCategory(item.name());
        }
        return category.get();
    }

    @PostMapping("/category/update")
    public StoreCategory updateCategory(@ModelAttribute StoreCategory item) {
        Optional<StoreCategory> category = categoryService.findById(item.getId());
        Optional<StoreCategory> existingCategory = categoryService.findByName(item.getName());

        if (existingCategory.isPresent()) {
            return item;
        }
        if (category.isPresent()) {
            category.get().setName(item.getName());
            return categoryService.save(category.get());
        }
        return item;
    }

    @PostMapping("/categories")
    public List<StoreCategory> getCategories() {
        return categoryService.findAll();
    }

    @PostMapping("/categoru")
    public StoreCategory getCategory(@ModelAttribute SingleBigIntegerModel item) {
        Optional<StoreCategory> category = categoryService.findById(item.id());
        if (category.isPresent()) {
            return category.get();
        }
        return new StoreCategory();
    }

    @PostMapping("/category/count")
    public ItemCount countStoreItemCategory(@ModelAttribute StoreCategory item) {
        Integer count = storeItemService.countByCategory(item.getId());
        return new ItemCount("category", count);
    }

    @PostMapping("/category/delete")
    public SingleBigIntegerModel deleteCategory(@ModelAttribute SingleBigIntegerModel item) {
        categoryService.deleteById(item.id());
        return item;
    }

    @PostMapping("/subcategories")
    public List<StoreSubCategory> getSubCategories(@ModelAttribute SingleBigIntegerModel item) {
        Optional<StoreCategory> category = categoryService.findById(item.id());
        if (category.isPresent()) {
            return category.get().getSubCategoryList();
        }
        return new ArrayList<>();
    }

    @PostMapping("/subcategory/edit")
    public StoreSubCategoryModel updateSubCategory(@ModelAttribute StoreSubCategoryModel item) {
        Optional<StoreSubCategory> subCategory = subCategoryService.findById(item.id());
        Optional<StoreCategory> category = categoryService.findById(item.parentId());

        if (subCategory.isPresent()) {
            subCategory.get().setName(item.name());
            subCategoryService.save(subCategory.get());
            return item;
        } else {
            if (category.isPresent()) {
                StoreSubCategory newSub = new StoreSubCategory(null, item.name(), category.get().getId());
                category.get().getSubCategoryList().add(subCategoryService.save(newSub));
                categoryService.save(category.get());
            }
        }
        return item;
    }

    @PostMapping("/subcategory/count")
    public ItemCount countStoreItemSubCategory(@ModelAttribute StoreSubCategory item) {
        Integer count = storeItemService.countBySubCategory(item.getId());
        return new ItemCount("subCategory", count);
    }

    @PostMapping("/subcategory/delete")
    public StoreSubCategory deleteSubCategory(@ModelAttribute StoreSubCategory item) {
        Optional<StoreSubCategory> subCategory = subCategoryService.findById(item.getId());
        if (subCategory.isPresent()) {
            subCategoryService.deleteById(item.getId());
        }
        return item;
    }


    @GetMapping("/options/{id}")
    public List<StoreItemOption> getOptions(@PathVariable BigInteger id) {
        Optional<StoreItem> storeItem = storeItemService.findById(id);
        return storeItem.get().getItemOptions();
    }

    @PostMapping("/options/edit/{id}")
    public StoreItemOption updateOption(@ModelAttribute StoreItemOption item, @PathVariable BigInteger id) {
        Optional<StoreItemOption> option = optionService.findById(item.getId());

        if (option.isPresent()) {
            option.get().setName(item.getName());
            return optionService.save(option.get());
        } else {
            Optional<StoreItem> storeItem = storeItemService.findById(id);
            if (storeItem.isPresent()) {
                storeItem.get().getItemOptions().add(new StoreItemOption(null, item.getName(),storeItem.get()));
                storeItemService.save(storeItem.get());
            }
            return item;
        }
    }

    @PostMapping("/options/delete/{id}")
    public StoreItemOption deleteOption(@ModelAttribute StoreItemOption item, @PathVariable BigInteger id) {
        Optional<StoreItem> storeItem = storeItemService.findById(id);

        if (storeItem.isPresent()) {
            for (StoreItemOption option : storeItem.get().getItemOptions()) {
                if (option.getId().equals(item.getId())) {
                    storeItem.get().getItemOptions().remove(option);
                    optionService.deleteById(option.getId());
                    break;
                }
            }
            storeItemService.save(storeItem.get());
        }
        return item;
    }

    @PostMapping("/storesetting/update")
    public String updateSettingNotify(@ModelAttribute SingleStringModel notifyModel, @ModelAttribute SingleBigIntegerModel userModel) {
        RequestNotifyTarget target = RequestNotifyTarget.valueOf(notifyModel.name());
        Optional<User> user = userService.findById(userModel.id());

        storeSettingService.setStoreNotifyTarget(target);
        if (target.equals(RequestNotifyTarget.User) && user.isPresent()) {
            storeSettingService.setUser(user.get());
        }

        return "true";
    }

    @GetMapping("/image/{id}")
    public StoreImage getStoreImage(@PathVariable BigInteger id) {
        Optional<StoreImage> image = storeImageService.findById(id);
        return image.orElseGet(StoreImage::new);

    }

    @PostMapping("/image/update")
    public StoreImage updateStoreImage(@ModelAttribute StoreImage img) {
        Optional<StoreImage> image = storeImageService.findById(img.getId());
        if (image.isPresent()) {
            image.get().setName(img.getName());
            image.get().setDescription(img.getDescription());
            storeImageService.save(image.get());
            return image.get();
        }
        return img;
    }


    @PostMapping("/item/delete")
    public SingleBigIntegerModel deleteStoreItem(@ModelAttribute SingleBigIntegerModel item) {
        if (allowByAdmin()) {
            storeItemService.deleteById(item.id());
            return item;
        }
        return new SingleBigIntegerModel(BigInteger.valueOf(0));
    }


    /*  Determine Edit Permissions */
    private boolean allowByAdmin(){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) auth.getPrincipal();
        Collection<UserRoles> roles = user.getUserRoles();

        for (UserRoles role : roles){
            if (role.getName().equals("ADMIN_WRITE") || role.getName().equals("STORE_SUPERVISOR")) {
                return true;
            }
        }
        return false;
    }
}
