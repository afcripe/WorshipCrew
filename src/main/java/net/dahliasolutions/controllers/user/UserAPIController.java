package net.dahliasolutions.controllers.user;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import net.dahliasolutions.models.BrowserMessage;
import net.dahliasolutions.models.UniversalSearchModel;
import net.dahliasolutions.models.department.DepartmentCampus;
import net.dahliasolutions.models.department.DepartmentRegional;
import net.dahliasolutions.models.mail.EmailDetails;
import net.dahliasolutions.models.order.OrderRequest;
import net.dahliasolutions.models.position.ChangeTemplateModel;
import net.dahliasolutions.models.position.PermissionTemplate;
import net.dahliasolutions.models.records.BigIntegerStringModel;
import net.dahliasolutions.models.records.CampusDepartmentModel;
import net.dahliasolutions.models.records.SingleStringModel;
import net.dahliasolutions.models.campus.Campus;
import net.dahliasolutions.models.user.User;
import net.dahliasolutions.models.user.UserRoles;
import net.dahliasolutions.services.AuthService;
import net.dahliasolutions.services.campus.CampusService;
import net.dahliasolutions.services.department.DepartmentCampusService;
import net.dahliasolutions.services.department.DepartmentRegionalService;
import net.dahliasolutions.services.mail.EmailService;
import net.dahliasolutions.services.position.PermissionTemplateService;
import net.dahliasolutions.services.user.UserRolesService;
import net.dahliasolutions.services.user.UserService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import javax.swing.text.html.Option;
import java.math.BigInteger;
import java.util.*;

@RestController
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
public class UserAPIController {

    private final UserService userService;
    private final UserRolesService userRolesService;
    private final CampusService campusService;
    private final DepartmentRegionalService departmentRegionalService;
    private final DepartmentCampusService departmentCampusService;
    private final PermissionTemplateService permissionTemplateService;
    private final AuthService authService;
    private final EmailService emailService;

    @GetMapping("")
    public List<User> goUserHome() {
        return userService.findAll();
    }

    @GetMapping ("/campus/{campus}")
    public List<User> getUserList(@PathVariable String campus) {
        Optional<Campus> campusOption = campusService.findByName(campus);
        return userService.findAllByCampus(campusOption.get());
    }

    @PostMapping("/setrole/{id}")
    public String setUserRole(@ModelAttribute SingleStringModel role, @PathVariable BigInteger id) {
        Optional<UserRoles> userRole = userRolesService.findByName(role.name());
        Optional<User> user = userService.findById(id);
        boolean found = false;
        if (userRole.isPresent() && user.isPresent()) {
            Collection<UserRoles> newRoles = new ArrayList<>();
            for (UserRoles r : user.get().getUserRoles()) {
                if (!r.getName().equals(userRole.get().getName())) {
                    newRoles.add(r);
                } else {
                    found = true;
                }
            }
            if (!found) {
                newRoles.add(userRole.get());
            }

            user.get().setUserRoles(newRoles);
            userService.save(user.get());
            return "true";
        }
        return "false";
    }

    @PostMapping("/settemplate")
    public String setUserRole(@ModelAttribute ChangeTemplateModel templateModel) {
        Optional<User> user = userService.findById(templateModel.userId());
        Optional<PermissionTemplate> template = permissionTemplateService.findById(templateModel.templateId());

        if (user.isPresent() && template.isPresent()) {
            user.get().setUserRoles(new ArrayList<>());

            for (UserRoles role : template.get().getUserRoles()) {
                user.get().getUserRoles().add(role);
            }
            userService.save(user.get());

            return "true";
        }
        return "false";
    }

    @PostMapping("/sendPasswordChange")
    public BigIntegerStringModel sendPasswordChangeRequest(@ModelAttribute BigIntegerStringModel userModel, HttpSession session) {
        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Optional<User> user = userService.findById(userModel.id());
        if(user.isEmpty()){
            session.setAttribute("msgError", "No User Found!");
            return new BigIntegerStringModel(BigInteger.valueOf(0), "");
        }


        // verify permission

        if (!authService.verifyUserPassword(currentUser, userModel.name())) {
            session.setAttribute("msgError", "Incorrect Admin Password!");
            return new BigIntegerStringModel(BigInteger.valueOf(0), "");
        }

        EmailDetails emailDetails = new EmailDetails(BigInteger.valueOf(0), user.get().getContactEmail(),"Password Change Request",
                "", null);
        BrowserMessage msg = emailService.sendPasswordResetMail(emailDetails, user.get().getId());

        session.setAttribute(msg.getMsgType(), msg.getMessage());

        return userModel;
    }

    @PostMapping("/filteredusers")
    public List<User> filteredUserList(@ModelAttribute CampusDepartmentModel filterModel) {
        List<User> userListReturn;
        Optional<Campus> campus = campusService.findByName(filterModel.campus());
        Optional<User> user = userService.findById(filterModel.userId());
        Optional<DepartmentRegional> regionalDep = departmentRegionalService.findByName(filterModel.department());

        Optional<User> director = Optional.empty();
        if (user.isPresent()) { director = userService.findById(regionalDep.get().getDirectorId()); }

        if (campus.isEmpty()) {
            if (regionalDep.isPresent()) {
                userListReturn = userService.findAllByDepartment(regionalDep.get());
            } else {
                userListReturn = userService.findAll();
            }

        } else {
            Optional<DepartmentCampus> campusDep = departmentCampusService.findByNameAndCampus(filterModel.department(), campus.get());
            if (campusDep.isPresent()) {
                userListReturn = userService.findAllByDepartmentCampus(campusDep.get());
            } else {
                userListReturn = userService.findAll();
            }
            // make sure campus director is available
            Optional<User> campDir = userService.findById(campusDep.get().getDirectorId());
            if (campDir.isPresent()) {
                if (!userListReturn.contains(campDir.get())) {
                    userListReturn.add(campDir.get());
                }
            }
        }

        // make sure regional director is available
        Optional<DepartmentRegional> dep = departmentRegionalService.findByName(filterModel.department());
        if (dep.isPresent()) {
            Optional<User> regDir = userService.findById(dep.get().getDirectorId());
            if (regDir.isPresent()) {
                if (!userListReturn.contains(regDir.get())) {
                    userListReturn.add(regDir.get());
                }
            }
        }

        // make sure current user director is present
        if (director.isPresent()) {
            if (!userListReturn.contains(director.get())) {
                userListReturn.add(director.get());
            }
        }

        // if (user.isPresent()) { userListReturn.add(user.get()); }

        Collections.sort(userListReturn, new Comparator<User>() {
            @Override
            public int compare(User user1, User user2) {
                return user1.getFullName().compareToIgnoreCase(user2.getFullName());
            }
        });

        return userListReturn;
    }

    @PostMapping("/search")
    public List<UniversalSearchModel> searchRequests(@ModelAttribute SingleStringModel stringModel) {
        // init return
        List<UniversalSearchModel> searchReturn = new ArrayList<>();

        List<User> foundUsers = userService.searchAllByFullName(stringModel.name());
        for (User user : foundUsers) {
            searchReturn.add(new UniversalSearchModel(user.getFullName(), "user", user.getId(), ""));
        }

        return searchReturn;
    }

    @GetMapping("/role/{id}")
    public UserRoles getUserRole(@PathVariable BigInteger id) {
        return userRolesService.findById(id).get();
    }

    @PostMapping("/role/update")
    public UserRoles setUserRole(@ModelAttribute UserRoles roleModel) {
        Optional<UserRoles> role = userRolesService.findById(roleModel.getId());
        if (role.isPresent()) {
            role.get().setDescription(roleModel.getDescription());
            userRolesService.save(role.get());
        }
        return roleModel;
    }

}
