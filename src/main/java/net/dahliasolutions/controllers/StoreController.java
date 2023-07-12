package net.dahliasolutions.controllers;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import net.dahliasolutions.data.StoreItemRepository;
import net.dahliasolutions.models.*;
import net.dahliasolutions.services.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/store")
@RequiredArgsConstructor
public class StoreController {

    private final StoredImageService storedImageService;
    private final PositionService positionService;
    private final DepartmentService departmentService;
    private final UserService userService;
    private final RedirectService redirectService;

    @GetMapping("")
    public String goStoreHome(Model model, HttpSession session) {
        redirectService.setHistory(session, "/store");
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

}
