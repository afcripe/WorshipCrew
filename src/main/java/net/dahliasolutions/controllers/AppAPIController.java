package net.dahliasolutions.controllers;

import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/v1/app")
public class AppAPIController {

    @GetMapping("/removeerrormsg")
    public void removeErrorMSG(HttpSession session) {
        try {
            session.removeAttribute("msgError");
            session.removeAttribute("msgSuccess");
        } catch (Exception e) {
            System.out.println("Attributes not removed.");
        }
    }

    @GetMapping("/removesuccessmsg")
    public void removeSuccessMSG(HttpSession session) {
        try {
            session.removeAttribute("msgError");
            session.removeAttribute("msgSuccess");
        } catch (Exception e) {
            System.out.println("Attributes not removed.");
        }
    }

    @GetMapping("/toggleSideNav/{sideNav}")
    public void toggleSideNav(@PathVariable boolean sideNav, HttpSession session) {
        if (sideNav) {
            session.setAttribute("showSideNav", true);
            session.removeAttribute("hideSideNav");
        } else {
            session.removeAttribute("showSideNav");
            session.setAttribute("hideSideNav", true);
        }
    }
}
