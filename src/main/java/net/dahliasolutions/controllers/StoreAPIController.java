package net.dahliasolutions.controllers;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import net.dahliasolutions.models.*;
import net.dahliasolutions.services.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/store")
@RequiredArgsConstructor
public class StoreAPIController {

    private final StoreItemService storeItemService;
    private final PositionService positionService;
    private final DepartmentService departmentService;
    private final UserService userService;
    private final StoredImageService storedImageService;
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
