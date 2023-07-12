package net.dahliasolutions.controllers;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import net.dahliasolutions.models.BrowserMessage;
import net.dahliasolutions.models.EmailDetails;
import net.dahliasolutions.models.LoginModel;
import net.dahliasolutions.models.User;
import net.dahliasolutions.services.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("")
@RequiredArgsConstructor
public class IndexController {

    private final UserService userService;
    private final EmailService emailService;
    private final RedirectService redirectService;

    @GetMapping("/")
    public String goHome(Model model, HttpSession session) {
        redirectService.setHistory(session, "/");
        return "index";
    }

    @GetMapping("/login")
    public String loginForm(){
        return "login";
    }

    @GetMapping("/forgotpassword")
    public String resetForm(){
        return "forgotPassword";
    }

    @PostMapping("/signin")
    public String processLogin(HttpSession session) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) auth.getPrincipal();
        session.setAttribute("userDisplayName", user.getFirstName());
        return "index";
    }

    @GetMapping("/logout")
    public String logout() {
        return "logout";
    }

    @PostMapping("/login")
    public String loginUser(@ModelAttribute LoginModel loginModel){
        return "redirect:/user/index";
    }

    @PostMapping("/passwordreset")
    public String processPasswordReset(@ModelAttribute LoginModel loginModel, HttpSession session) {
        User user = userService.findByUsername(loginModel.getUsername()).orElse(null);
        if(user == null){
            session.setAttribute("msgError", "Username not Found!");
            return "forgotPassword";
        }
        EmailDetails emailDetails =
                new EmailDetails(user.getContactEmail(),"Password Reset", "", null );

        BrowserMessage returnMsg = emailService.sendPasswordResetMail(emailDetails, user.getId());
        session.setAttribute(returnMsg.getMsgType(), returnMsg.getMessage());
        return "redirect:/";
    }

}
