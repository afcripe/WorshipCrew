package net.dahliasolutions.controllers.user;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import net.dahliasolutions.models.*;
import net.dahliasolutions.models.campus.Campus;
import net.dahliasolutions.models.department.DepartmentCampus;
import net.dahliasolutions.models.department.DepartmentRegional;
import net.dahliasolutions.models.mail.EmailDetails;
import net.dahliasolutions.models.position.PermissionTemplate;
import net.dahliasolutions.models.position.Position;
import net.dahliasolutions.models.user.*;
import net.dahliasolutions.services.*;
import net.dahliasolutions.services.campus.CampusService;
import net.dahliasolutions.services.department.DepartmentCampusService;
import net.dahliasolutions.services.department.DepartmentRegionalService;
import net.dahliasolutions.services.mail.EmailService;
import net.dahliasolutions.services.position.PermissionTemplateService;
import net.dahliasolutions.services.position.PositionService;
import net.dahliasolutions.services.user.UserRolesService;
import net.dahliasolutions.services.user.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.swing.text.html.Option;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final UserRolesService rolesService;
    private final PositionService positionService;
    private final AuthService authService;
    private final CampusService campusService;
    private final DepartmentRegionalService departmentRegionalService;
    private final DepartmentCampusService departmentCampusService;
    private final PermissionTemplateService permissionTemplateService;
    private final EmailService emailService;
    private final RedirectService redirectService;

    @ModelAttribute
    public void addAttributes(Model model) {
        model.addAttribute("moduleTitle", "Settings");
        model.addAttribute("moduleLink", "/admin");
    }

    @GetMapping("")
    public String getUsers(Model model, HttpSession session) {
        // get permissions
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) auth.getPrincipal();
        Collection<UserRoles> roles = currentUser.getUserRoles();
        boolean fullList = false;
        for (UserRoles role : roles){
            if (role.getName().equals("ADMIN_WRITE") || role.getName().equals("ADMIN_READ") ||
                    role.getName().equals("DIRECTOR_WRITE") || role.getName().equals("DIRECTOR_READ")) {
                fullList = true;
                break;
            }
        }

        String title = "All Users";
        List<User> userList = new ArrayList<>();
        if (fullList) {
            userList = userService.findAll();
        } else {
            userList = userService.findAllByCampus(currentUser.getCampus());
            title = "Campus Users";
        }

        redirectService.setHistory(session, "/user");
        model.addAttribute("title", title);
        model.addAttribute("users", userList);
        return "admin/user/listUsers";
    }

    @GetMapping("/viewdeleted")
    public String getDeletedUsers(Model model, HttpSession session) {
        // get permissions
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) auth.getPrincipal();
        Collection<UserRoles> roles = currentUser.getUserRoles();
        boolean fullList = false;
        for (UserRoles role : roles){
            if (role.getName().equals("ADMIN_WRITE") || role.getName().equals("ADMIN_READ") ||
                    role.getName().equals("DIRECTOR_WRITE") || role.getName().equals("DIRECTOR_READ")) {
                fullList = true;
                break;
            }
        }

        List<User> userList = new ArrayList<>();
        if (fullList) {
            userList = userService.findAllByDeleted(true);
        } else {
            userList = userService.findAllByCampusAndDeleted(currentUser.getCampus(), true);
        }

        redirectService.setHistory(session, "/user");
        model.addAttribute("title", "All Users");
        model.addAttribute("users", userList);
        return "admin/user/listDeletedUsers";
    }

    @GetMapping("/{id}")
    public String getUser(@PathVariable BigInteger id, Model model, HttpSession session) {
        Optional<User> user = userService.findById(id);
        if (user.isEmpty()) {
            session.setAttribute("msgError", "User Not Found!");
            return redirectService.pathName(session, "user");
        }
        redirectService.setHistory(session, "/user/"+id);

        boolean authResourceRead = false;
        boolean authResourceWrite = false;
        boolean authStoreRead = false;
        boolean authStoreWrite = false;
        boolean authSupportWrite = false;
        for (UserRoles role : user.get().getUserRoles()) {
            switch (role.getName()){
                case "RESOURCE_READ":
                    authResourceRead = true;
                    break;
                case "RESOURCE_WRITE":
                    authResourceWrite = true;
                    break;
                case "STORE_READ":
                    authStoreRead = true;
                    break;
                case "STORE_WRITE":
                    authStoreWrite = true;
                    break;
                case "SUPPORT_WRITE":
                    authSupportWrite = true;
                    break;
            }
        }


        model.addAttribute("user", user.get());
        model.addAttribute("userCampus", user.get().getCampus().getName());
        model.addAttribute("userDepartment", user.get().getDepartment().getName());
        model.addAttribute("userPosition", user.get().getPosition().getName());
        model.addAttribute("userPosition", user.get().getPosition().getName());

        model.addAttribute("authResourceRead", authResourceRead);
        model.addAttribute("authResourceWrite", authResourceWrite);
        model.addAttribute("authStoreRead", authStoreRead);
        model.addAttribute("authStoreWrite", authStoreWrite);
        model.addAttribute("authSupportWrite", authSupportWrite);
        return "admin/user/user";
    }

    @GetMapping("/{id}/edit")
    public String updateUserForm(@PathVariable BigInteger id, Model model, HttpSession session) {

        Optional<User> user = userService.findById(id);
        if (user.isEmpty()) {
            session.setAttribute("msgError", "User Not Found!");
            return redirectService.pathName(session, "user");
        }

        // get persmissions
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) auth.getPrincipal();
        Collection<UserRoles> roles = currentUser.getUserRoles();
        boolean fullList = false;
        for (UserRoles role : roles){
            if (role.getName().equals("ADMIN_WRITE") || role.getName().equals("ADMIN_READ") ||
                    role.getName().equals("DIRECTOR_WRITE") || role.getName().equals("DIRECTOR_READ")) {
                fullList = true;
                break;
            }
        }

        if (user.get().getPosition().getLevel() < currentUser.getPosition().getLevel()) {
            session.setAttribute("msgError", "Permission to Edit User Denied!");
            return redirectService.pathName(session, "user");
        }


        List<Campus> campusList = new ArrayList<>();
        List<User> userList = new ArrayList<>();
        List<Position> positionList = new ArrayList<>();
        if (fullList) {
            campusList = campusService.findAll();
            userList = userService.findAll();
            positionList = positionService.findAll();
        } else {
            campusList.add(currentUser.getCampus());
            userList = userService.findAllByCampus(currentUser.getCampus());
            positionList = positionService.findAllByLevelGreaterThan(currentUser.getPosition().getLevel());
        }

        List<DepartmentRegional> departmentList = departmentRegionalService.findAll();

        model.addAttribute("user", user.get());
        model.addAttribute("userCampus", user.get().getCampus().getName());
        model.addAttribute("userDepartment", user.get().getDepartment().getName());
        model.addAttribute("userPosition", user.get().getPosition().getName());
        model.addAttribute("campusList", campusList);
        model.addAttribute("departmentList", departmentList);
        model.addAttribute("positionList", positionList);
        model.addAttribute("userList", userList);

        return "admin/user/userEdit";
    }

    @PostMapping("/update")
    public String updateUserResult(@ModelAttribute UserModel userModel, Model model, HttpSession session) {
        Optional<User> user = userService.findById(userModel.id());
        Optional<User> exists = userService.findByUsername(userModel.username());

        if (user.isEmpty()) {
            session.setAttribute("msgError", "User Not Found!");
            return redirectService.pathName(session, "user");
        }
        if (exists.isPresent() && !exists.get().getId().equals(userModel.id())) {
            session.setAttribute("msgError", "Username Already Exists!");
            return redirectService.pathName(session, "user");
        }

        boolean updatePermissions = false;
        if (!user.get().getPosition().getName().equals(positionService.findByName(userModel.position()).get().getName())) {
            updatePermissions = true;
        }

        user.get().setUsername(userModel.username());

        user.get().setFirstName(userModel.firstName());
        user.get().setLastName(userModel.lastName());
        user.get().setContactEmail(userModel.contactEmail());
        user.get().setPosition(positionService.findByName(userModel.position()).orElse(null));
        user.get().setCampus(campusService.findByName(userModel.campus()).orElse(null));
        user.get().setDepartment(departmentCampusService.findByNameAndCampus(userModel.department(), user.get().getCampus()).orElse(null));
        user.get().setDirector(userService.findById(userModel.directorId()).orElse(null));
        userService.save(user.get());

        // set permissions
        if (updatePermissions) {
            user.get().setUserRoles(new ArrayList<>());
            Optional<PermissionTemplate> template = permissionTemplateService.findFirstByPosition(user.get().getPosition());
            if (template.isPresent()) {
                for (UserRoles role : template.get().getUserRoles()) {
                    user.get().getUserRoles().add(role);
                }
            }
        }


        model.addAttribute("user", user);
        session.setAttribute("msgSuccess", "User successfully updated.");

        return "redirect:/user/"+userModel.id().toString();
    }

    @GetMapping("/new")
    public String newUserForm(Model model) {
        // get persmissions
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) auth.getPrincipal();
        Collection<UserRoles> roles = currentUser.getUserRoles();
        boolean fullList = false;
        for (UserRoles role : roles){
            if (role.getName().equals("ADMIN_WRITE") || role.getName().equals("ADMIN_READ") ||
                    role.getName().equals("DIRECTOR_WRITE") || role.getName().equals("DIRECTOR_READ")) {
                fullList = true;
                break;
            }
        }

        List<Campus> campusList = new ArrayList<>();
        List<User> userList = new ArrayList<>();
        List<Position> positionList = new ArrayList<>();
        if (fullList) {
            campusList = campusService.findAll();
            userList = userService.findAll();
            positionList = positionService.findAll();
        } else {
            campusList.add(currentUser.getCampus());
            userList = userService.findAllByCampus(currentUser.getCampus());
            positionList = positionService.findAllByLevelGreaterThan(currentUser.getPosition().getLevel());
        }

        User user = new User();
        List<DepartmentRegional> departmentList = departmentRegionalService.findAll();

        model.addAttribute("user", user);
        model.addAttribute("positionList", positionList);
        model.addAttribute("campusList", campusList);
        model.addAttribute("departmentList", departmentList);
        model.addAttribute("userList", userList);
        return "admin/user/userNew";
    }

    @PostMapping("/create")
    public String createUser(@ModelAttribute UserModel userModel, Model model, HttpSession session) {
        if(userService.verifyByUsername(userModel.username())) {
            model.addAttribute("registerUser", userModel);
            session.setAttribute("msgError", "Username already exists!");
            return "admin/user/userNew";
        }
        User newUser;
        User user = new User();
            user.setUsername(userModel.username());
            user.setFirstName(userModel.firstName());
            user.setLastName(userModel.lastName());
            user.setContactEmail(userModel.username());
            user.setPosition(positionService.findByName(userModel.position()).orElse(null));
            user.setCampus(campusService.findByName(userModel.campus()).orElse(null));
            user.setDepartment(departmentCampusService.findByNameAndCampus(userModel.department(), user.getCampus()).orElse(null));
            user.setDirector(userService.findById(userModel.directorId()).orElse(null));
            user.setUserRoles(new ArrayList<>());

        // set permissions
        Optional<PermissionTemplate> template = permissionTemplateService.findFirstByPosition(user.getPosition());
        if (template.isPresent()) {
            for (UserRoles role : template.get().getUserRoles()) {
                user.getUserRoles().add(role);
            }
        }

        if (userModel.activated() == null){
            user.setPassword(userModel.password());
            userService.createDefaultUser(user);
        } else {
            userService.createUser(user);
            EmailDetails emailDetails = new EmailDetails(user.getContactEmail(),"",
                    "Welcome to Destiny Worship Exchange", null);
            emailService.sendWelcomeMail(emailDetails, user.getId());
        }

        model.addAttribute("user", user);
        session.setAttribute("msgSuccess", "User successfully added.");

        return "redirect:/user/"+user.getId().toString();
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

        return "admin/user/userAddPermission";
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

        return "admin/user/userSetPassword";
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

    @GetMapping("/{id}/delete")
    public String deletedUser(@PathVariable BigInteger id, HttpSession session) {
        Optional<User> user = userService.findById(id);
        if (user.isPresent()) {
            if (!user.get().isDeleted()) {
                user.get().setUserRoles(new ArrayList<>());
                user.get().setDeleted(true);
                userService.save(user.get());
            }
        }
        return redirectService.pathName(session, "user");
    }

    @GetMapping("/{id}/restore")
    public String restoreUser(@PathVariable BigInteger id, HttpSession session) {
        Optional<User> user = userService.findById(id);
        if (user.isPresent()) {
            if (user.get().isDeleted()) {
                user.get().setDeleted(false);
                // set permissions based on position
                Optional<PermissionTemplate> template = permissionTemplateService.findFirstByPosition(user.get().getPosition());
                if (template.isPresent()) {
                    for (UserRoles role : template.get().getUserRoles()) {
                        user.get().getUserRoles().add(role);
                    }
                }
            }
            userService.save(user.get());
        }
        return redirectService.pathName(session, "user");
    }
}
