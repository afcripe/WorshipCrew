package net.dahliasolutions.controllers;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import net.dahliasolutions.models.*;
import net.dahliasolutions.services.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.swing.text.html.Option;
import java.math.BigInteger;
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
    private final DepartmentService departmentService;
    private final UserService userService;
    private final StoredImageService storedImageService;
    private final RedirectService redirectService;

    @ModelAttribute
    public void addAttributes(Model model) {
        model.addAttribute("moduleTitle", "Store");
        model.addAttribute("moduleLink", "/store");
    }

    @GetMapping("")
    public String goStoreHome(Model model, HttpSession session) {
        redirectService.setHistory(session, "/store");
        List<StoreItem> itemList = storeItemService.findAll();
        model.addAttribute("storeItems", itemList);
        return "store/index";
    }

    @GetMapping("/new")
    public String getNewItem(Model model) {

        StoreItem storeItem = new StoreItem();
        List<User> userList = userService.findAll();
        List<Department> departmentList = departmentService.findAll();
        List<Position> positionList = positionService.findAll();

        model.addAttribute("storeItem", storeItem);
        model.addAttribute("userList", userList);
        model.addAttribute("positionList", positionList);
        model.addAttribute("departmentList", departmentList);
        return "store/itemNew";
    }

    @PostMapping("/create")
    public String createStoreItem(@ModelAttribute StoreItemModel storeItemModel, Model model, HttpSession session) {
        if (storeItemModel.name().equals("")) {
            List<User> userList = userService.findAll();
            List<Department> departmentList = departmentService.findAll();
            List<Position> positionList = positionService.findAll();

            model.addAttribute("storeItem", storeItemModel);
            model.addAttribute("userList", userList);
            model.addAttribute("positionList", positionList);
            model.addAttribute("departmentList", departmentList);
            session.setAttribute("msgError", "Product Name is Required.");
            return "store/itemNew";
        }

        boolean specialOrder = storeItemModel.specialOrder() != null;
        boolean available = storeItemModel.available() != null;

        StoreItem storeItem = new StoreItem();
        storeItem.setName(storeItemModel.name());
        storeItem.setDescription(storeItemModel.description());
        storeItem.setCount(storeItemModel.count());
        storeItem.setSpecialOrder(specialOrder);
        storeItem.setAvailable(available);
        storeItem.setLeadTime(storeItemModel.leadTime());
        if (storeItemModel.department() != null) {
            storeItem.setDepartment(departmentService.findById(storeItemModel.department()).orElse(null));
        }
        if (storeItemModel.owner() != null) {
            storeItem.setOwner(userService.findById(storeItemModel.owner()).orElse(null));
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
        Optional<StoreItem> storeItem = storeItemService.findById(id);
        if (storeItem.isEmpty()) {
            session.setAttribute("msgError", "Item not found.");
        }
        model.addAttribute("storeItem", storeItem.get());
        return "store/item";
    }

    @GetMapping("/item/edit/{id}")
    public String getItemToEdit(@PathVariable BigInteger id, Model model, HttpSession session) {

        Optional<StoreItem> storeItem = storeItemService.findById(id);
        if (storeItem.isEmpty()) {
            session.setAttribute("msgError", "Item not found.");
            return redirectService.pathName(session, "/store");
        }

        List<User> userList = userService.findAll();
        List<Department> departmentList = departmentService.findAll();
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

        model.addAttribute("storeItem", storeItem.get());
        model.addAttribute("userList", userList);
        model.addAttribute("positionList", positionList);
        model.addAttribute("departmentList", departmentList);
        return "store/itemEdit";
    }

    @PostMapping("/update")
    public String updateStoreItem(@ModelAttribute StoreItemModel storeItemModel, Model model, HttpSession session) {
        Optional<StoreItem> storeItem = storeItemService.findById(storeItemModel.id());
        if (storeItem.isEmpty()) {
            session.setAttribute("msgError", "Item not found..");
            return redirectService.pathName(session, "/store");
        }
        if (storeItemModel.name().equals("")) {
            List<User> userList = userService.findAll();
            List<Department> departmentList = departmentService.findAll();
            List<Position> positionList = positionService.findAll();

            model.addAttribute("storeItem", storeItemModel);
            model.addAttribute("userList", userList);
            model.addAttribute("positionList", positionList);
            model.addAttribute("departmentList", departmentList);
            session.setAttribute("msgError", "Product Name is Required.");
            return "/item/edit/"+storeItemModel.id();
        }

        boolean specialOrder = storeItemModel.specialOrder() != null;
        boolean available = storeItemModel.available() != null;

        storeItem.get().setName(storeItemModel.name());
        storeItem.get().setDescription(storeItemModel.description());
        storeItem.get().setCount(storeItemModel.count());
        storeItem.get().setSpecialOrder(specialOrder);
        storeItem.get().setAvailable(available);
        storeItem.get().setLeadTime(storeItemModel.leadTime());
        if (storeItemModel.department() != null) {
            storeItem.get().setDepartment(departmentService.findById(storeItemModel.department()).orElse(null));
        }
        if (storeItemModel.owner() != null) {
            storeItem.get().setOwner(userService.findById(storeItemModel.owner()).orElse(null));
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
    public String getStoreSettings() {
        return "store/settings";
    }

    @GetMapping("/imageManager")
    public String getImageManager(Model model, HttpSession session) {
        redirectService.setHistory(session, "/store/settings");
        List<StoredImage> storedImageList = storedImageService.findAll();
        List<StoredImageModel> imageList = new ArrayList<>();
        for (StoredImage image : storedImageList) {
            StoredImageModel imageModel = new StoredImageModel(image.getId(), image.getName(), image.getDescription(),
                    image.getFileLocation(), 0);
            imageList.add(imageModel);
        }

        List<StoreItem> storeItemList = storeItemService.findAll();
        for (StoreItem storeItem : storeItemList) {
            for (StoredImageModel img : imageList) {
                if (storeItem.getId().equals(img.getId())) {
                    img.setReferences(img.getReferences()+1);
                }
            }
        }

        model.addAttribute("imageList", imageList);
        return "store/imageManager";
    }
}
