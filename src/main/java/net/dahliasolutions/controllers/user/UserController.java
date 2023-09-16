package net.dahliasolutions.controllers.user;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import net.dahliasolutions.models.*;
import net.dahliasolutions.models.campus.Campus;
import net.dahliasolutions.models.department.DepartmentCampus;
import net.dahliasolutions.models.department.DepartmentRegional;
import net.dahliasolutions.models.mail.EmailDetails;
import net.dahliasolutions.models.order.OrderStatus;
import net.dahliasolutions.models.position.PermissionTemplate;
import net.dahliasolutions.models.position.Position;
import net.dahliasolutions.models.records.CampusDepartmentModel;
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
import java.util.*;

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
    private final AdminSettingsService adminService;
    private final EventService eventService;

    @ModelAttribute
    public void addAttributes(Model model) {
        model.addAttribute("moduleTitle", "Users");
        model.addAttribute("moduleLink", "/user");
    }

    @GetMapping("")
    public String getUsers(Model model, HttpSession session) {
        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String permission = permissionType(currentUser);

        List<User> userList = userList(currentUser);
        Collections.sort(userList, new Comparator<User>() {
            @Override
            public int compare(User user1, User user2) {
                return user1.getFullName().compareToIgnoreCase(user2.getFullName());
            }
        });

        model.addAttribute("title", permission);
        model.addAttribute("users", userList);
        model.addAttribute("userEdit", userWrite(currentUser));

        redirectService.setHistory(session, "/user");
        if (permission.equals("All Users")) {
            model.addAttribute("campusList", campusService.findAll());
            model.addAttribute("departmentList", departmentRegionalService.findAll());
            model.addAttribute("selectedCampus", "");
            model.addAttribute("selectedDepartment", "");
            return "user/userAdmin";
        }
        return "user/listUsers";
    }

    @GetMapping(value = "", params = {"campus"})
    public String getUsersFilterCampus(@RequestParam String campus, Model model, HttpSession session) {
        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String permission = permissionType(currentUser);
        List<User> userList = userList(currentUser);

        // only filter if all users permission
        if (permission.equals("All Users")) {
            Optional<Campus> filteredCampus = campusService.findByName(campus);
            if (filteredCampus.isPresent()) {
                userList = userService.findAllByCampus(filteredCampus.get());
            }
        }
        Collections.sort(userList, new Comparator<User>() {
            @Override
            public int compare(User user1, User user2) {
                return user1.getFullName().compareToIgnoreCase(user2.getFullName());
            }
        });

        model.addAttribute("title", permission);
        model.addAttribute("users", userList);
        model.addAttribute("userEdit", userWrite(currentUser));

        redirectService.setHistory(session, "/user");
        if (permission.equals("All Users")) {
            model.addAttribute("campusList", campusService.findAll());
            model.addAttribute("departmentList", departmentRegionalService.findAll());
            model.addAttribute("selectedCampus", campus);
            model.addAttribute("selectedDepartment", "");
            return "user/userAdmin";
        }
        return "user/listUsers";
    }

    @GetMapping(value = "", params = {"department"})
    public String getUsersFilterDepartment(@RequestParam String department, Model model, HttpSession session) {
        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String permission = permissionType(currentUser);
        List<User> userList = userList(currentUser);

        // only filter if all users permission
        if (permission.equals("All Users")) {
            Optional<DepartmentRegional> filteredDepartment = departmentRegionalService.findByName(department);
            if (filteredDepartment.isPresent()) {
                userList = userService.findAllByDepartment(filteredDepartment.get());
            }
        }
        Collections.sort(userList, new Comparator<User>() {
            @Override
            public int compare(User user1, User user2) {
                return user1.getFullName().compareToIgnoreCase(user2.getFullName());
            }
        });

        model.addAttribute("title", permission);
        model.addAttribute("users", userList);
        model.addAttribute("userEdit", userWrite(currentUser));

        redirectService.setHistory(session, "/user");
        if (permission.equals("All Users")) {
            model.addAttribute("campusList", campusService.findAll());
            model.addAttribute("departmentList", departmentRegionalService.findAll());
            model.addAttribute("selectedCampus", "");
            model.addAttribute("selectedDepartment", department);
            return "user/userAdmin";
        }
        return "user/listUsers";
    }

    @GetMapping(value = "", params = {"campus", "department"})
    public String getUsersFilterCampusDepartment(@RequestParam String campus, @RequestParam String department, Model model, HttpSession session) {
        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String permission = permissionType(currentUser);
        List<User> userList = userList(currentUser);;

        // only filter if all users permission
        if (permission.equals("All Users")) {
            Optional<Campus> filteredCampus = campusService.findByName(campus);
            if (filteredCampus.isPresent()) {
                Optional<DepartmentCampus> filteredDepartment = departmentCampusService.findByNameAndCampus(department, filteredCampus.get());
                if (filteredDepartment.isPresent()) {
                    userList = userService.findAllByDepartmentCampus(filteredDepartment.get());
                }
            }

        }
        Collections.sort(userList, new Comparator<User>() {
            @Override
            public int compare(User user1, User user2) {
                return user1.getFullName().compareToIgnoreCase(user2.getFullName());
            }
        });

        model.addAttribute("title", permission);
        model.addAttribute("users", userList);
        model.addAttribute("userEdit", userWrite(currentUser));

        redirectService.setHistory(session, "/user");
        if (permission.equals("All Users")) {
            model.addAttribute("campusList", campusService.findAll());
            model.addAttribute("departmentList", departmentRegionalService.findAll());
            model.addAttribute("selectedCampus", campus);
            model.addAttribute("selectedDepartment", department);
            return "user/userAdmin";
        }
        return "user/listUsers";
    }

    @GetMapping("/viewdeleted")
    public String getDeletedUsers(Model model, HttpSession session) {
        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String permission = permissionType(currentUser);
        List<User> userList = userList(currentUser);
        Collections.sort(userList, new Comparator<User>() {
            @Override
            public int compare(User user1, User user2) {
                return user1.getFirstName().compareToIgnoreCase(user2.getFirstName());
            }
        });

        model.addAttribute("title", permission);
        model.addAttribute("users", userList);

        redirectService.setHistory(session, "/user");

        return "user/listDeletedUsers";
    }

    @GetMapping("/{id}")
    public String getUser(@PathVariable BigInteger id, Model model, HttpSession session) {
        Optional<User> user = userService.findById(id);
        if (user.isEmpty()) {
            session.setAttribute("msgError", "User Not Found!");
            return redirectService.pathName(session, "user");
        }

        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

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

        List<PermissionTemplate> templateList = getPermissionTemplates(currentUser);

        model.addAttribute("user", user.get());
        model.addAttribute("userCampus", user.get().getCampus().getName());
        model.addAttribute("userDepartment", user.get().getDepartment().getName());
        model.addAttribute("userPosition", user.get().getPosition().getName());
        model.addAttribute("userPosition", user.get().getPosition().getName());
        model.addAttribute("templateList", templateList);

        model.addAttribute("authResourceRead", authResourceRead);
        model.addAttribute("authResourceWrite", authResourceWrite);
        model.addAttribute("authStoreRead", authStoreRead);
        model.addAttribute("authStoreWrite", authStoreWrite);
        model.addAttribute("authSupportWrite", authSupportWrite);
        model.addAttribute("userEdit", userEdit(currentUser, user.get()));

        redirectService.setHistory(session, "/user/"+id);
        return "user/user";
    }

    @GetMapping("/edit/{id}")
    public String updateUserForm(@PathVariable BigInteger id, Model model, HttpSession session) {
        Optional<User> user = userService.findById(id);
        if (user.isEmpty()) {
            session.setAttribute("msgError", "User Not Found!");
            return redirectService.pathName(session, "user");
        }

        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        boolean editeUser = userEdit(currentUser, user.get());
        if (!editeUser) {
            session.setAttribute("msgError", "Permission to Edit User Denied!");
            return redirectService.pathName(session, "user");
        }

        model.addAttribute("user", user.get());
        model.addAttribute("userCampus", user.get().getCampus().getName());
        model.addAttribute("userDepartment", user.get().getDepartment().getName());
        model.addAttribute("userPosition", user.get().getPosition().getName());

        model.addAttribute("campusList", campusList(currentUser));
        model.addAttribute("departmentList", departmentList(currentUser));
        model.addAttribute("positionList", positionList(currentUser));
        model.addAttribute("userList", filteredUserList(user.get(), user.get().getDirector()));

        return "user/userEdit";
    }

    @PostMapping("/update")
    public String updateUserResult(@ModelAttribute UserModel userModel, Model model, HttpSession session) {
        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
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

        model.addAttribute("user", user.get());
        session.setAttribute("msgSuccess", "User successfully updated.");

        // send any additional notifications
        String userFullName = currentUser.getFirstName()+" "+currentUser.getLastName();
        String theUser = user.get().getFirstName()+" "+user.get().getLastName();
        String eventName = "User "+theUser+" has been updated";
        String eventDesc = "User "+theUser+" was updated by "+userFullName;
        // update
        Event e = new Event(null, eventName, eventDesc, user.get().getId(), "", EventModule.User, EventType.Changed);
        eventService.dispatchEvent(e);

        return "redirect:/user/"+userModel.id().toString();
    }

    @GetMapping("/new")
    public String newUserForm(Model model) {
        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        List<User> userList = userList(currentUser);
        Collections.sort(userList, new Comparator<User>() {
            @Override
            public int compare(User user1, User user2) {
                return user1.getFirstName().compareToIgnoreCase(user2.getFirstName());
            }
        });

        User user = new User();

        model.addAttribute("user", user);
        model.addAttribute("campusList", campusList(currentUser));
        model.addAttribute("departmentList", departmentList(currentUser));
        model.addAttribute("positionList", positionList(currentUser));
        model.addAttribute("userList", userList(currentUser));
        return "user/userNew";
    }

    @PostMapping("/create")
    public String createUser(@ModelAttribute UserModel userModel, Model model, HttpSession session) {
        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if(userService.verifyByUsername(userModel.username())) {
            model.addAttribute("registerUser", userModel);
            session.setAttribute("msgError", "Username already exists!");
            return "user/userNew";
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

        // Send Password E-mail
        AdminSettings adminSettings = adminService.getAdminSettings();
        userService.createUser(user);
        EmailDetails emailDetails = new EmailDetails(user.getContactEmail(),
                "Welcome to " + adminSettings.getCompanyName(),
                "Welcome to " + adminSettings.getCompanyName(), null);
        BrowserMessage msg = emailService.sendWelcomeMail(emailDetails, user.getId());

        model.addAttribute("user", user);
        session.setAttribute("msgSuccess", "User successfully added.");

        // send any additional notifications
        String userFullName = currentUser.getFirstName()+" "+currentUser.getLastName();
        String theUser = user.getFirstName()+" "+user.getLastName();
        String eventName = "New User "+theUser+" has been added";
        String eventDesc = "User "+theUser+" was added by "+userFullName;
        // new
        Event e = new Event(null, eventName, eventDesc, user.getId(), "", EventModule.User, EventType.New);
        eventService.dispatchEvent(e);

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

        return "user/userAddPermission";
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
        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Optional<User> user = userService.findById(id);
        if(user.isEmpty()){
            session.setAttribute("msgError", "No User Found!");
            return "redirect:/user/"+id.toString();
        }
        if(!userEdit(currentUser, user.get())) {
            session.setAttribute("msgError", "Permission Denied!");
            return "redirect:/user/"+id.toString();
        }

        model.addAttribute("user", user.get());

        return "user/userSetPassword";
    }

    @PostMapping("/changepassword")
    public String changeUserPassword(@ModelAttribute ChangePasswordModel changePasswordModel, HttpSession session) {
        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Optional<User> user = userService.findById(changePasswordModel.id());
        if(user.isEmpty()){
            session.setAttribute("msgError", "No User Found!");
            return "redirect:/user/"+changePasswordModel.id().toString();
        }

        String pwdTemplate = "/user/"+changePasswordModel.id().toString()+"/changepassword";
        // verify permission
        if (!userEdit(currentUser, user.get())) {
            session.setAttribute("msgError", "Access Denied!");
            return "redirect:"+pwdTemplate;
        }
        if (!authService.verifyUserPassword(currentUser,changePasswordModel.currentPassword())) {
            session.setAttribute("msgError", "Incorrect Admin Password!");
            return "redirect:"+pwdTemplate;
        }
        // Verify passwords match
        if (!changePasswordModel.newPassword().equals(changePasswordModel.confirmPassword())) {
            session.setAttribute("msgError", "Passwords Do Not Match!");
            return "redirect:"+pwdTemplate;
        }

        userService.updateUserPasswordById(changePasswordModel.id(), changePasswordModel.newPassword());
        session.setAttribute("msgSuccess", "Password Change Successful.");

        // send any additional notifications
        String userFullName = currentUser.getFirstName()+" "+currentUser.getLastName();
        String theUser = user.get().getFirstName()+" "+user.get().getLastName();
        String eventName = "User "+theUser+" password was changed";
        String eventDesc = "User "+theUser+" password was changed by "+userFullName;
        // changed
        Event e = new Event(null, eventName, eventDesc, user.get().getId(), "", EventModule.User, EventType.Changed);
        eventService.dispatchEvent(e);

        return "redirect:/user/"+changePasswordModel.id();
    }

    @GetMapping("/delete/{id}")
    public String deletedUser(@PathVariable BigInteger id, HttpSession session) {
        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Optional<User> user = userService.findById(id);
        if (user.isPresent()) {
            if (!user.get().isDeleted()) {
                user.get().setUserRoles(new ArrayList<>());
                user.get().setDeleted(true);
                userService.save(user.get());
            }
        }

        // send any additional notifications
        String userFullName = currentUser.getFirstName()+" "+currentUser.getLastName();
        String theUser = user.get().getFirstName()+" "+user.get().getLastName();
        String eventName = "User "+theUser+" was deleted";
        String eventDesc = "User "+theUser+" was deleted by "+userFullName;
        // changed
        Event e = new Event(null, eventName, eventDesc, user.get().getId(), "", EventModule.User, EventType.Changed);
        eventService.dispatchEvent(e);
        // deleted
        e.setType(EventType.Deleted);
        eventService.dispatchEvent(e);

        return redirectService.pathName(session, "user");
    }

    @GetMapping("/restore/{id}")
    public String restoreUser(@PathVariable BigInteger id, HttpSession session) {
        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
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

        // send any additional notifications
        String userFullName = currentUser.getFirstName()+" "+currentUser.getLastName();
        String theUser = user.get().getFirstName()+" "+user.get().getLastName();
        String eventName = "User "+theUser+" was restored";
        String eventDesc = "User "+theUser+" was restored by "+userFullName;
        // changed
        Event e = new Event(null, eventName, eventDesc, user.get().getId(), "", EventModule.User, EventType.Changed);
        eventService.dispatchEvent(e);

        return redirectService.pathName(session, "user");
    }

    @PostMapping("/search")
    public String searchRequests(@ModelAttribute UniversalSearchModel searchModel) {
        // determine if search type
        switch (searchModel.getSearchType()) {
            case "user":
                return "redirect:/user/"+searchModel.getSearchId();
            default:
                return "redirect:/user";
        }
    }

    // get edit permission
    private boolean userWrite(User currentUser){
        Collection<UserRoles> roles = currentUser.getUserRoles();
        for (UserRoles role : roles){
            if (role.getName().equals("ADMIN_WRITE") || role.getName().equals("USER_WRITE")
                    || role.getName().equals("USER_SUPERVISOR")) {
                return true;
            }
        }
        return false;
    }

    private boolean userEdit(User currentUser, User user){
        List<User> userList = userList(currentUser);
        Collection<UserRoles> roles = currentUser.getUserRoles();
        for (UserRoles role : roles){
            if (role.getName().equals("ADMIN_WRITE") || role.getName().equals("USER_SUPERVISOR")) {
                return true;
            }
            if (role.getName().equals("USER_WRITE")) {
                if (userList.contains(user) && user.getPosition().getLevel() > currentUser.getPosition().getLevel()) {
                    return true;
                }
            }
        }
        return false;
    }

    private String permissionType(User currentUser) {
        Collection<UserRoles> roles = currentUser.getUserRoles();
        String typeString = "Campus Users";
        int priority = 0;
        for (UserRoles role : roles) {
            if (role.getName().equals("USER_WRITE") || role.getName().equals("USER_READ")) {
                if (priority < 1) {
                    typeString = "Campus Department Users";
                    priority = 1;
                }
            } else if (role.getName().equals("CAMPUS_WRITE") || role.getName().equals("CAMPUS_READ")) {
                if (priority < 2) {
                    typeString = "Campus Users";
                    priority = 2;
                }
            }   else if (role.getName().equals("DIRECTOR_WRITE") || role.getName().equals("DIRECTOR_READ")) {
                if (priority < 3) {
                    typeString = "Department Users";
                    priority = 3;
                }
            } else if (role.getName().equals("ADMIN_WRITE") || role.getName().equals("USER_SUPERVISOR")) {
                if (priority < 4) {
                    typeString = "All Users";
                    priority = 4;
                }
            }
        }
        return typeString;
    }

    private List<User> userList(User currentUser) {
        String permission = permissionType(currentUser);
        List<User> userList;
        switch (permission){
            case "All Users":
                userList = userService.findAll();
                break;
            case "Department Users":
                userList = userService.findAllByDepartmentAndDeleted(currentUser.getDepartment().getRegionalDepartment(), false);
                break;
            case "Campus Users":
                userList = userService.findAllByCampus(currentUser.getCampus());
                break;
            default:
                userList = userService.findAllByDepartmentCampus(currentUser.getDepartment());
        }
        return userList;
    }

    private List<Position> positionList(User currentUser) {
        List<Position> positionList;
        Collection<UserRoles> roles = currentUser.getUserRoles();
        for (UserRoles role : roles){
            if (role.getName().equals("ADMIN_WRITE") || role.getName().equals("USER_SUPERVISOR")) {
                positionList = positionService.findAll();
                return positionList;
            }
        }
        return positionService.findAllByLevelGreaterThan(currentUser.getPosition().getLevel());
    }

    private List<Campus> campusList(User currentUser) {
        List<Campus> campusList = new ArrayList<>();
        Collection<UserRoles> roles = currentUser.getUserRoles();
        for (UserRoles role : roles){
            if (role.getName().equals("ADMIN_WRITE") || role.getName().equals("USER_SUPERVISOR")
                    || role.getName().equals("DIRECTOR_WRITE") || role.getName().equals("DIRECTOR_READ")) {
                campusList = campusService.findAll();
                return campusList;
            }
        }
        campusList.add(currentUser.getCampus());
        return campusList;
    }

    private List<DepartmentRegional> departmentList(User currentUser) {
        List<DepartmentRegional> departmentList = new ArrayList<>();
        Collection<UserRoles> roles = currentUser.getUserRoles();
        for (UserRoles role : roles){
            if (role.getName().equals("ADMIN_WRITE") || role.getName().equals("USER_SUPERVISOR")
                    || role.getName().equals("CAMPUS_WRITE") || role.getName().equals("CAMPUS_READ")) {
                departmentList = departmentRegionalService.findAll();
                return departmentList;
            }
        }
        departmentList.add(currentUser.getDepartment().getRegionalDepartment());
        return departmentList;
    }

    private List<User> filteredUserList(User user, User director) {
        // init return
        List<User> userListReturn ;
        DepartmentRegional department = user.getDepartment().getRegionalDepartment();
        Optional<User> depDirector = userService.findById(department.getDirectorId());

        Optional<DepartmentCampus> campusDep = departmentCampusService.findByNameAndCampus(department.getName(), user.getCampus());
        if (campusDep.isPresent()) {
            userListReturn = userService.findAllByDepartmentCampus(campusDep.get());
            if (depDirector.isPresent()) {
                if (!userListReturn.contains(depDirector.get())) {
                    userListReturn.add(depDirector.get());
                }
            }
        } else {
            userListReturn = userService.findAllByDepartment(department);
        }

        if (director != null) { userListReturn.add(director); }

        Collections.sort(userListReturn, new Comparator<User>() {
            @Override
            public int compare(User user1, User user2) {
                return user1.getFullName().compareToIgnoreCase(user2.getFullName());
            }
        });

        return userListReturn;
    }

    private List<User> filteredUserList(Campus campus, DepartmentRegional department, User user) {
        // init return
        List<User> userListReturn;
        Optional<User> director = userService.findById(department.getDirectorId());

        if (campus == null) {
            userListReturn = userService.findAllByDepartment(department);
            if (director.isPresent()) {
                if (!userListReturn.contains(director.get())) {
                    userListReturn.add(director.get());
            }
            }
        } else {
            Optional<DepartmentCampus> campusDep = departmentCampusService.findByNameAndCampus(department.getName(), campus);
            if (campusDep.isPresent()) {
                userListReturn = userService.findAllByDepartmentCampus(campusDep.get());
                if (director.isPresent()) {
                    if (!userListReturn.contains(director.get())) {
                        userListReturn.add(director.get());
                    }
                }
            } else {
                userListReturn = userService.findAllByDepartment(department);
            }
        }

        if (user != null) { userListReturn.add(user); }

        Collections.sort(userListReturn, new Comparator<User>() {
            @Override
            public int compare(User user1, User user2) {
                return user1.getFullName().compareToIgnoreCase(user2.getFullName());
            }
        });

        return userListReturn;
    }

    private List<PermissionTemplate> getPermissionTemplates(User currentUser) {
        List<PermissionTemplate> templateList = permissionTemplateService.findAll();
        List<PermissionTemplate> returnList = new ArrayList<>();
        for (PermissionTemplate perm : templateList) {
            if (perm.getPosition().getLevel() > currentUser.getPosition().getLevel()) {
                returnList.add(perm);
            }
        }

        Collections.sort(returnList, new Comparator<PermissionTemplate>() {
            @Override
            public int compare(PermissionTemplate perm1, PermissionTemplate perm2) {
                return perm1.getName().compareToIgnoreCase(perm2.getName());
            }
        });
        return returnList;
    }

}
