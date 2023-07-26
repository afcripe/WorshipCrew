package net.dahliasolutions.controllers;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import net.dahliasolutions.models.user.ChangePasswordModel;
import net.dahliasolutions.models.user.User;
import net.dahliasolutions.models.user.UserModel;
import net.dahliasolutions.services.AuthService;
import net.dahliasolutions.services.RedirectService;
import net.dahliasolutions.services.user.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@Controller
@RequestMapping("/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final UserService userService;
    private final AuthService authService;
    private final RedirectService redirectService;

    @ModelAttribute
    public void addAttributes(Model model) {
        model.addAttribute("moduleTitle", "Profile");
        model.addAttribute("moduleLink", "/profile");
    }

    @GetMapping("")
    public String getUser(Model model, HttpSession session) {
        redirectService.setHistory(session, "/profile");
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User loggedInAuth = (User) auth.getPrincipal();
        User user = userService.findByUsername((loggedInAuth.getUsername())).orElse(new User());

        model.addAttribute("user", user);
        model.addAttribute("userPosition", user.getPosition().getName());
        return "profile/index";
    }

    @GetMapping("/changepassword")
    public String changeUserPasswordForm(Model model, HttpSession session) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User loggedInAuth = (User) auth.getPrincipal();
        User user = userService.findByUsername((loggedInAuth.getUsername())).orElse(null);

        if(user == null){
            session.setAttribute("msgError", "No User Found!");
            return "redirect:/profile";
        }

        model.addAttribute("user", user);

        return "profile/setPassword";
    }

    @PostMapping("/changepassword")
    public String changeUserPassword(@ModelAttribute ChangePasswordModel changePasswordModel, HttpSession session) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User loggedInAuth = (User) auth.getPrincipal();
        User user = userService.findByUsername((loggedInAuth.getUsername())).orElse(null);

        if(user == null){
            session.setAttribute("msgError", "No User Found!");
            return "redirect:/profile";
        }

        // Verify Current Password
        boolean isAuth = authService.verifyUserPassword(user, changePasswordModel.currentPassword());

        if(!isAuth) {
            session.setAttribute("msgError", "Incorrect Password!");
            return "redirect:/profile/changepassword";
        } else if (!changePasswordModel.newPassword().equals(changePasswordModel.confirmPassword())) {
            session.setAttribute("msgError", "Passwords Do Not Match!");
            return "redirect:/profile/changepassword";
        } else {
            userService.updateUserPasswordById(user.getId(), changePasswordModel.newPassword());
            session.setAttribute("msgSuccess", "Password Change Successful.");
        }
        return "redirect:/profile";
    }

    @GetMapping("/edit")
    public String updateProfileForm(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User loggedInAuth = (User) auth.getPrincipal();
        User user = userService.findByUsername((loggedInAuth.getUsername())).orElse(null);

        model.addAttribute("user", user);

        return "profile/profileEdit";
    }

    @PostMapping("/update")
    public String updateProfileResult(@ModelAttribute UserModel userModel, Model model, HttpSession session) {
        User user = userService.findById(userModel.id()).orElse(null);
        if (user != null) {
            user.setFirstName(userModel.firstName());
            user.setLastName(userModel.lastName());
            user.setContactEmail(userModel.contactEmail());
            userService.save(user);
        }

        model.addAttribute("user", user);
        session.setAttribute("msgSuccess", "User successfully updated.");

        return "redirect:/profile";
    }

    @GetMapping("/changeusername")
    public String updateUsernameForm(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User loggedInAuth = (User) auth.getPrincipal();
        User user = userService.findByUsername((loggedInAuth.getUsername())).orElse(null);

        model.addAttribute("user", user);

        return "profile/usernameEdit";
    }

    @PostMapping("/changeusername")
    public String updateProfileUsernameResult(@ModelAttribute UserModel userModel, Model model, HttpSession session) {
        Optional<User> user = userService.findById(userModel.id());
        User exists = userService.findByUsername(userModel.username()).orElse(new User());

        if (user.isEmpty()) {
            session.setAttribute("msgError", "Username Not Found!");
            return "profile/usernameEdit";
        }else{
            if(user.get().getId().equals(exists.getId())) {
                model.addAttribute("user", user);
                session.setAttribute("msgError", "Username unchanged!");
                return "profile/usernameEdit";
            } else if (userModel.username().equals(exists.getUsername())) {
                model.addAttribute("user", user);
                session.setAttribute("msgError", "Username Already Exists!");
                return "profile/usernameEdit";
            }

            user.get().setUsername(userModel.username());
            userService.save(user.get());

            session.setAttribute("msgSuccess", "Username successfully updated. Pleas login with your new username.");

            return "redirect:/login";
        }

    }
}
