package net.dahliasolutions.controllers;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import net.dahliasolutions.models.*;
import net.dahliasolutions.services.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigInteger;
import java.util.List;

@Controller
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final UserRolesService rolesService;
    private final PositionService positionService;
    private final AuthService authService;
    private final CampusService campusService;
    private final DepartmentService departmentService;
    private final EmailService emailService;
    private final RedirectService redirectService;

    @GetMapping("")
    public String getUsers(Model model, HttpSession session) {
        redirectService.setHistory(session, "/user");
        List<User> userList = userService.findAll();
        model.addAttribute("title", "All Users");
        model.addAttribute("users", userList);
        return "admin/listUsers";
    }

    @GetMapping("/{id}")
    public String getUser(@PathVariable BigInteger id, Model model, HttpSession session) {
        redirectService.setHistory(session, "/user/"+id);
        User user = userService.findById(id).orElse(new User());

        model.addAttribute("user", user);
        model.addAttribute("userCampus", user.getCampus().getName());
        model.addAttribute("userDepartment", user.getDepartment().getName());
        model.addAttribute("userPosition", user.getPosition().getName());
        return "admin/user";
    }

    @GetMapping("/{id}/edit")
    public String updateUserForm(@PathVariable BigInteger id, Model model) {
        User user = userService.findById(id).orElse(new User());
        List<Position> positionList = positionService.findAll();
        List<Campus> campusList = campusService.findAll();
        List<Department> departmentList = departmentService.findAll();

        model.addAttribute("user", user);
        model.addAttribute("userCampus", user.getCampus().getName());
        model.addAttribute("userDepartment", user.getDepartment().getName());
        model.addAttribute("userPosition", user.getPosition().getName());
        model.addAttribute("campusList", campusList);
        model.addAttribute("departmentList", departmentList);
        model.addAttribute("positionList", positionList);

        return "admin/userEdit";
    }

    @PostMapping("/update")
    public String updateUserResult(@ModelAttribute UserModel userModel, Model model, HttpSession session) {
        User user = userService.findById(userModel.id()).orElse(null);
        User exists = userService.findByUsername(userModel.username()).orElse(new User());

        if (user != null) {
            if(!user.getId().equals(exists.getId())) {
                if(userModel.username().equals(exists.getUsername())) {
                    session.setAttribute("msgError", "Username Already Exists!");
                } else {
                    user.setUsername(userModel.username());
                }
            }
            user.setFirstName(userModel.firstName());
            user.setLastName(userModel.lastName());
            user.setContactEmail(userModel.contactEmail());
            user.setPosition(positionService.findByName(userModel.position()).orElse(null));
            user.setCampus(campusService.findByName(userModel.campus()).orElse(null));
            user.setDepartment(departmentService.findByName(userModel.department()).orElse(null));
            userService.save(user);
        }

        model.addAttribute("user", user);
        session.setAttribute("msgSuccess", "User successfully updated.");

        return "redirect:/user/"+userModel.id().toString();
    }

    @GetMapping("/new")
    public String newUserForm(Model model) {
        User user = new User();
        List<Position> positionList = positionService.findAll();
        List<Campus> campusList = campusService.findAll();
        List<Department> departmentList = departmentService.findAll();

        model.addAttribute("user", user);
        model.addAttribute("positionList", positionList);
        model.addAttribute("campusList", campusList);
        model.addAttribute("departmentList", departmentList);
        return "admin/userNew";
    }

    @PostMapping("/create")
    public String createUser(@ModelAttribute UserModel userModel, Model model, HttpSession session) {
        if(userService.verifyByUsername(userModel.username())) {
            model.addAttribute("registerUser", userModel);
            session.setAttribute("msgError", "Username already exists!");
            return "admin/userNew";
        }
        User newUser;
        User user = new User();
            user.setUsername(userModel.username());
            user.setFirstName(userModel.firstName());
            user.setLastName(userModel.lastName());
            user.setContactEmail(userModel.username());
            user.setPosition(positionService.findByName(userModel.position()).orElse(null));
            user.setCampus(campusService.findByName(userModel.campus()).orElse(null));
            user.setDepartment(departmentService.findByName(userModel.department()).orElse(null));

        if (userModel.activated() == null){
            user.setPassword(userModel.password());
            newUser = userService.createDefaultUser(user);
        } else {
            newUser = userService.createUser(user);
            EmailDetails emailDetails = new EmailDetails(newUser.getContactEmail(),"",
                    "Welcome to Destiny Worship Exchange", null);
            emailService.sendWelcomeMail(emailDetails, newUser.getId());
        }

        model.addAttribute("user", newUser);
        session.setAttribute("msgSuccess", "User successfully added.");

        return "redirect:/user/"+newUser.getId().toString();
    }

    @GetMapping("/{id}/removerole/{roleName}")
    public String removeUserRole(@PathVariable BigInteger id, @PathVariable String roleName, HttpSession session) {
        String modifiedStr = roleName.replaceAll("\"", "");

        // Verify user has permission
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) auth.getPrincipal();
        boolean isAuth = authService.isAuthorized(user.getUsername(), "ADMIN_WRITE");

        if(isAuth) {
            userService.removeRoleFromUser(id, modifiedStr);
            session.setAttribute("msgSuccess", "Permission Removed.");
        } else {
            session.setAttribute("msgError", "Access Denied!");
        }
        return "redirect:/user/"+id.toString();
    }

    @GetMapping("/{id}/addpermission")
    public String addUserRoleForm(@PathVariable BigInteger id, Model model, HttpSession session) {
        User user = userService.findById(id).orElse(null);
        if(user == null){
            session.setAttribute("msgError", "No User Found!");
            return "redirect:/user/"+id.toString();
        }

        List<UserRoles> roles = rolesService.findAll();

        model.addAttribute("user", user);
        model.addAttribute("rolesList", roles);

        return "admin/userAddPermission";
    }

    @PostMapping("/addpermission")
    public String addUserRole(@ModelAttribute UserRolesModel userRolesModel, HttpSession session) {
        User user = userService.findById(userRolesModel.id()).orElse(null);
        if(user == null){
            session.setAttribute("msgError", "No User Found!");
            return "redirect:/user/"+userRolesModel.id().toString();
        }

        // Verify user has permission
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User admin = (User) auth.getPrincipal();
        boolean isAuth = authService.isAuthorized(admin.getUsername(), "ADMIN_WRITE");

        if(isAuth) {
            userService.addRoleToUser(user.getUsername(), userRolesModel.role());
            session.setAttribute("msgSuccess", "Permission Added.");
        } else {
            session.setAttribute("msgError", "Access Denied!");
        }
        return "redirect:/user/"+userRolesModel.id().toString();
    }

    @GetMapping("/{id}/changepassword")
    public String changeUserPasswordForm(@PathVariable BigInteger id, Model model, HttpSession session) {
        User user = userService.findById(id).orElse(null);
        if(user == null){
            session.setAttribute("msgError", "No User Found!");
            return "redirect:/user/"+id.toString();
        }

        model.addAttribute("user", user);

        return "admin/userSetPassword";
    }

    @PostMapping("/changepassword")
    public String changeUserPassword(@ModelAttribute ChangePasswordModel changePasswordModel, HttpSession session) {
        User user = userService.findById(changePasswordModel.id()).orElse(null);
        if(user == null){
            session.setAttribute("msgError", "No User Found!");
            return "redirect:/user/"+changePasswordModel.id().toString();
        }

        // Verify Admin has permission
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User admin = (User) auth.getPrincipal();
        LoginModel loginModel = new LoginModel(admin.getUsername(), changePasswordModel.currentPassword());
        boolean isAuth = authService.verifyAuthorizedByPassword(loginModel,"ADMIN_WRITE");
        String pwdTemplate = "/user/"+changePasswordModel.id().toString()+"/changepassword";
        if(!isAuth) {
            session.setAttribute("msgError", "Access Denied!");
            return "redirect:"+pwdTemplate;
        } else if (!changePasswordModel.newPassword().equals(changePasswordModel.confirmPassword())) {
            session.setAttribute("msgError", "Passwords Do Not Match!");
            return "redirect:"+pwdTemplate;
        } else {
            userService.updateUserPasswordById(changePasswordModel.id(), changePasswordModel.newPassword());
            session.setAttribute("msgSuccess", "Password Change Successful.");
        }
        return "redirect:/user/"+changePasswordModel.id();
    }

    @PostMapping("/{id}/sendPasswordChange")
    public String sendPasswordChangeRequest(@PathVariable BigInteger id, HttpSession session) {
        User user = userService.findById(id).orElse(null);
        if(user == null){
            session.setAttribute("msgError", "No User Found!");
            return "redirect:/user/"+id.toString();
        }

        EmailDetails emailDetails = new EmailDetails(user.getContactEmail(),"",
                "Password Change Request", null);
        BrowserMessage msg = emailService.sendPasswordResetMail(emailDetails, user.getId());

        session.setAttribute(msg.getMsgType(), msg.getMessage());

        return "redirect:/user/"+user.getId().toString();
    }
}
