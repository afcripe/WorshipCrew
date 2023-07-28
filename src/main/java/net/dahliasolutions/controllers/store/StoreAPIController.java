package net.dahliasolutions.controllers.store;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import net.dahliasolutions.models.SingleBigIntegerModel;
import net.dahliasolutions.models.SingleStringModel;
import net.dahliasolutions.models.store.*;
import net.dahliasolutions.models.user.Profile;
import net.dahliasolutions.models.user.User;
import net.dahliasolutions.services.store.StoreCategoryService;
import net.dahliasolutions.services.store.StoreItemService;
import net.dahliasolutions.services.store.StoreSubCategoryService;
import net.dahliasolutions.services.user.ProfileService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
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

    @GetMapping("")
    public List<StoreItem> goStoreHome() {
        return storeItemService.findAll();
    }

    @PostMapping("/new")
    public SingleBigIntegerModel postNewStoreItem(@ModelAttribute SingleStringModel item) {
        StoreItem storeItem = new StoreItem();
        storeItem.setName(item.name());
        return new SingleBigIntegerModel(storeItemService.createStoreItem(storeItem).getId());
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

    @PostMapping("/category/new")
    public StoreCategory postNewCategory(@ModelAttribute SingleStringModel item) {
        Optional<StoreCategory> category = categoryService.findByName(item.name());
        if (category.isEmpty()) {
            return categoryService.createCategory(item.name());
        }
        return category.get();
    }

    @PostMapping("/category")
    public List<StoreSubCategory> getSubCategories(@ModelAttribute SingleBigIntegerModel item) {
        Optional<StoreCategory> category = categoryService.findById(item.id());
        if (category.isPresent()) {
            return category.get().getSubCategoryList();
        }
        return new ArrayList<>();
    }

    @PostMapping("/category/delete")
    public SingleBigIntegerModel deleteCategory(@ModelAttribute SingleBigIntegerModel item) {
        categoryService.deleteById(item.id());
        return item;
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
                StoreSubCategory newSub = new StoreSubCategory(null, item.name(), category.get());
                category.get().getSubCategoryList().add(subCategoryService.save(newSub));
                categoryService.save(category.get());
            }
        }
        return item;
    }

    @PostMapping("/category/count")
    public ItemCount countStoreItemCategory(@ModelAttribute StoreCategory item) {
        Integer count = storeItemService.countByCategory(item.getId());
        return new ItemCount("category", count);
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

}
