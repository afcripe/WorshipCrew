package net.dahliasolutions.controllers;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import net.dahliasolutions.models.*;
import net.dahliasolutions.services.MailerLinksService;
import net.dahliasolutions.services.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Controller
@RequiredArgsConstructor
@RequestMapping("/mailer")
public class EmailController {

    private final MailerLinksService mailerLinksService;
    private final UserService userService;

    @GetMapping("")
    public String testMailer(){
        return "support";
    }

    @GetMapping("/{randomString}")
    public String getSetPasswordForm(@PathVariable String randomString,
                                     @RequestParam Optional<BigInteger> eventId,
                                     @RequestParam Optional<String> action,
                                     @RequestParam Optional<String> eventList,
                                     Model model) {
        MailerLinks mailerLinks = mailerLinksService.findByRandomLinkString(randomString).orElse(null);
        if (mailerLinks == null) {
            model.addAttribute("errorMessage", "Link not Valid!");
            return "error";
        }
        if (mailerLinks.getExpiration().isBefore(LocalDateTime.now()) || mailerLinks.isForceExpire()) {
            model.addAttribute("errorMessage", " The link has Expired!");
            return "error";
        }

        User user = userService.findById((mailerLinks.getUserId())).orElse(null);
        model.addAttribute("userDetails", user);
        model.addAttribute("randomString", randomString);

        switch (mailerLinks.getServiceMethod()) {
            case "setPassword":
            case "resetPassword":
                return "mailer/setPassword";
            default:
                model.addAttribute("errorMessage", "Link not Valid!");
                return "error";
        }
    }

    @PostMapping("/setpassword")
    public String setNewPassword(@ModelAttribute ChangePasswordModel changePasswordModel, HttpSession session, Model model) {
        MailerLinks mailerLinks = mailerLinksService.findByRandomLinkString(changePasswordModel.currentPassword()).orElse(null);
        if (mailerLinks == null) {
            model.addAttribute("errorMessage", "Link not Valid!");
            return "error";
        }
        if (mailerLinks.getExpiration().isBefore(LocalDateTime.now()) || mailerLinks.isForceExpire()) {
            model.addAttribute("errorMessage", "Link has Expired!");
            return "error";
        }
        if (mailerLinks.getServiceMethod().equals("setPassword") || mailerLinks.getServiceMethod().equals("resetPassword")) {
            User user = userService.findById((mailerLinks.getUserId())).orElse(null);
            if(user == null){
                model.addAttribute("errorMessage", "User Not Found");
                return "error";
            }

            if (!changePasswordModel.newPassword().equals(changePasswordModel.confirmPassword())) {
                session.setAttribute("msgError", "Passwords Do Not Match!");
                return "redirect:/mailer/"+changePasswordModel.currentPassword();
            } else {
                userService.updateActivateUserPasswordById(user.getId(), changePasswordModel.newPassword());
                mailerLinks.setForceExpire(true);
                mailerLinksService.save(mailerLinks);
            }
            return "redirect:/login";
        }

        model.addAttribute("errorMessage", "Link not valid for setting password!");
        return "error";
    }

}
