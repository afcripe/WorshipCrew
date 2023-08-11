package net.dahliasolutions.controllers;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import net.dahliasolutions.models.user.Profile;
import net.dahliasolutions.models.user.User;
import net.dahliasolutions.services.user.ProfileService;
import net.dahliasolutions.services.user.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/app")
public class AppAPIController {

    private final ProfileService profileService;
    private final UserService userService;

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

    @GetMapping("/datefilter/{name}")
    public int setDateFilter(@PathVariable String name, HttpSession session) {
        try {
            session.setAttribute("dateFilter", name);
        } catch (Exception e) {
            System.out.println("Attributenot added.");
        }
        return 1;
    }

    @GetMapping("/toggleSideNav/{sideNav}")
    public void toggleSideNav(@PathVariable String sideNav, HttpSession session) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) auth.getPrincipal();
        Optional<Profile> profile = profileService.findByUser(user);
        profile.get().setSideNavigation(sideNav);
        profileService.save(profile.get());

        session.setAttribute("sideNavigation", sideNav);
    }

    @GetMapping("/toggletheme")
    public void toggleSideNav(HttpSession session) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) auth.getPrincipal();
        Optional<Profile> profile = profileService.findByUser(user);
        if (profile.get().getTheme().equals("default")) {
            profile.get().setTheme("dark");
        } else {
            profile.get().setTheme("default");

        }
        profileService.save(profile.get());

        session.setAttribute("theme", profile.get().getTheme());
    }

}
