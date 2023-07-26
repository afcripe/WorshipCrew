package net.dahliasolutions.controllers.store;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import net.dahliasolutions.models.store.StoreItem;
import net.dahliasolutions.services.*;
import net.dahliasolutions.services.department.DepartmentRegionalService;
import net.dahliasolutions.services.position.PositionService;
import net.dahliasolutions.services.store.StoreImageService;
import net.dahliasolutions.services.store.StoreItemService;
import net.dahliasolutions.services.user.UserService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/store")
@RequiredArgsConstructor
public class StoreAPIController {

    private final StoreItemService storeItemService;
    private final PositionService positionService;
    private final DepartmentRegionalService departmentService;
    private final UserService userService;
    private final StoreImageService storedImageService;
    private final RedirectService redirectService;

    @GetMapping("")
    public List<StoreItem> goStoreHome() {
        return storeItemService.findAll();
    }

    @GetMapping ("/display/{style}")
    public void toggleListGrid(@PathVariable String style, HttpSession session) {
        if (style.equals("grid")) {
            session.setAttribute("storeListGrid", "grid");
        } else {
            session.setAttribute("storeListGrid", "list");
        }
    }
}
