package net.dahliasolutions.controllers.app;

import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import net.dahliasolutions.models.*;
import net.dahliasolutions.models.campus.Campus;
import net.dahliasolutions.models.department.DepartmentRegional;
import net.dahliasolutions.models.mail.EmailDetails;
import net.dahliasolutions.models.position.PermissionTemplate;
import net.dahliasolutions.models.position.Position;
import net.dahliasolutions.models.records.BigIntegerStringModel;
import net.dahliasolutions.models.records.SingleBigIntegerModel;
import net.dahliasolutions.models.records.SingleStringModel;
import net.dahliasolutions.models.user.User;
import net.dahliasolutions.models.user.UserModel;
import net.dahliasolutions.models.user.UserRoles;
import net.dahliasolutions.services.AdminSettingsService;
import net.dahliasolutions.services.EventService;
import net.dahliasolutions.services.JwtService;
import net.dahliasolutions.services.RedirectService;
import net.dahliasolutions.services.campus.CampusService;
import net.dahliasolutions.services.department.DepartmentCampusService;
import net.dahliasolutions.services.department.DepartmentRegionalService;
import net.dahliasolutions.services.mail.EmailService;
import net.dahliasolutions.services.position.PermissionTemplateService;
import net.dahliasolutions.services.position.PositionService;
import net.dahliasolutions.services.user.UserRolesService;
import net.dahliasolutions.services.user.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigInteger;
import java.util.*;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/app/users")
public class MobileAppAPIUserController {

    private final JwtService jwtService;
    private final UserService userService;
    private final UserRolesService userRolesService;
    private final PositionService positionService;
    private final CampusService campusService;
    private final DepartmentRegionalService departmentRegionalService;
    private final DepartmentCampusService departmentCampusService;
    private final PermissionTemplateService permissionTemplateService;
    private final EmailService emailService;
    private final AdminSettingsService adminService;
    private final EventService eventService;

    @GetMapping("/")
    public ResponseEntity<List<User>> getUserHome(HttpServletRequest request) {
        APIUser apiUser = getUserFromToken(request);
        if (!apiUser.isValid()) {
            return new ResponseEntity<>(new ArrayList<>(), HttpStatus.FORBIDDEN);
        }

//        List<User> userList = userService.findAll();
        List<User> userList = userList(apiUser.getUser());

        Collections.sort(userList, new Comparator<User>() {
            @Override
            public int compare(User user1, User user2) {
                return user1.getFullName().compareToIgnoreCase(user2.getFullName());
            }
        });

        return new ResponseEntity<>(userList, HttpStatus.OK);
    }
    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable BigInteger id, HttpServletRequest request) {
        APIUser apiUser = getUserFromToken(request);
        if (!apiUser.isValid()) {
            return new ResponseEntity<>(new User(), HttpStatus.FORBIDDEN);
        }

        Optional<User> user = userService.findById(id);
        if (user.isEmpty()) {
            return new ResponseEntity<>(new User(), HttpStatus.BAD_REQUEST);
        }


        return new ResponseEntity<>(user.get(), HttpStatus.OK);
    }

    @GetMapping("/directorof/{id}")
    public ResponseEntity<User> getUserDirectorByUserId(@PathVariable BigInteger id, HttpServletRequest request) {
        APIUser apiUser = getUserFromToken(request);
        if (!apiUser.isValid()) {
            return new ResponseEntity<>(new User(), HttpStatus.FORBIDDEN);
        }

        Optional<User> user = userService.findById(id);
        if (user.isEmpty()) {
            return new ResponseEntity<>(new User(), HttpStatus.BAD_REQUEST);
        }


        return new ResponseEntity<>(user.get().getDirector(), HttpStatus.OK);
    }

    @GetMapping("/editpermission")
    public ResponseEntity<SingleStringModel> getEditPermission(HttpServletRequest request) {
        APIUser apiUser = getUserFromToken(request);
        if (!apiUser.isValid()) {
            return new ResponseEntity<>(new SingleStringModel(""), HttpStatus.FORBIDDEN);
        }

        Collection<UserRoles> roles = apiUser.getUser().getUserRoles();
        for (UserRoles role : roles){
            if (role.getName().equals("ADMIN_WRITE")) {
                return new ResponseEntity<>(new SingleStringModel("ADMIN_WRITE"), HttpStatus.OK);
            }
            if (role.getName().equals("USER_SUPERVISOR")) {
                return new ResponseEntity<>(new SingleStringModel("USER_SUPERVISOR"), HttpStatus.OK);
            }
            if (role.getName().equals("USER_WRITE")) {
                return new ResponseEntity<>(new SingleStringModel("USER_WRITE"), HttpStatus.OK);
            }
        }

        return new ResponseEntity<>(new SingleStringModel(""), HttpStatus.FORBIDDEN);
    }

    @GetMapping("/editpermission/{id}")
    public ResponseEntity<SingleStringModel> getEditPermissionForUserId(@PathVariable BigInteger id, HttpServletRequest request) {
        APIUser apiUser = getUserFromToken(request);
        if (!apiUser.isValid()) {
            return new ResponseEntity<>(new SingleStringModel(""), HttpStatus.FORBIDDEN);
        }

        Optional<User> user = userService.findById(id);
        if (user.isEmpty()) {
            return new ResponseEntity<>(new SingleStringModel(""), HttpStatus.BAD_REQUEST);
        }

        Collection<UserRoles> roles = apiUser.getUser().getUserRoles();
        for (UserRoles role : roles){
            if (role.getName().equals("ADMIN_WRITE")) {
                return new ResponseEntity<>(new SingleStringModel("ADMIN_WRITE"), HttpStatus.OK);
            }
            if (role.getName().equals("USER_SUPERVISOR")) {
                return new ResponseEntity<>(new SingleStringModel("USER_SUPERVISOR"), HttpStatus.OK);
            }
            if (role.getName().equals("USER_WRITE")) {
                if (user.get().getPosition().getLevel() > apiUser.getUser().getPosition().getLevel()) {
                    return new ResponseEntity<>(new SingleStringModel("USER_WRITE"), HttpStatus.OK);
                }
            }
        }

        return new ResponseEntity<>(new SingleStringModel(""), HttpStatus.FORBIDDEN);
    }

    @GetMapping("/appuser")
    public ResponseEntity<User> getEditPermissionForUserId(HttpServletRequest request) {
        APIUser apiUser = getUserFromToken(request);
        if (!apiUser.isValid()) {
            return new ResponseEntity<>(new User(), HttpStatus.FORBIDDEN);
        }

        return new ResponseEntity<>(apiUser.getUser(), HttpStatus.OK);
    }

    @PostMapping("/updateauth")
    public ResponseEntity<SingleStringModel> getEditPermissionForUserId(@ModelAttribute BigIntegerStringModel authModel, HttpServletRequest request) {
        APIUser apiUser = getUserFromToken(request);
        if (!apiUser.isValid()) {
            return new ResponseEntity<>(new SingleStringModel("failure"), HttpStatus.FORBIDDEN);
        }

        Optional<UserRoles> newRole = userRolesService.findByName(authModel.name());
        Optional<User> user = userService.findById(authModel.id());

        if (newRole.isPresent() && user.isPresent()) {
            if (user.get().getUserRoles().contains(newRole.get())) {
                // remove role
                user.get().getUserRoles().remove(newRole.get());
            } else {
                // add role
                user.get().getUserRoles().add(newRole.get());
            }
            userService.save(user.get());
        }

        return new ResponseEntity<>(new SingleStringModel("success"), HttpStatus.OK);
    }

    @GetMapping("/listcampus")
    public ResponseEntity<List<Campus>> getCampusList(HttpServletRequest request) {
        APIUser apiUser = getUserFromToken(request);
        if (!apiUser.isValid()) {
            return new ResponseEntity<>(new ArrayList<>(), HttpStatus.FORBIDDEN);
        }

        return new ResponseEntity<>(campusList(apiUser.getUser()), HttpStatus.OK);
    }

    @GetMapping("/listepartment")
    public ResponseEntity<List<DepartmentRegional>> getDepartmentList(HttpServletRequest request) {
        APIUser apiUser = getUserFromToken(request);
        if (!apiUser.isValid()) {
            return new ResponseEntity<>(new ArrayList(), HttpStatus.FORBIDDEN);
        }

        return new ResponseEntity<>(departmentList(apiUser.getUser()), HttpStatus.OK);
    }

    @GetMapping("/listeposition")
    public ResponseEntity<List<Position>> getPositionList(HttpServletRequest request) {
        APIUser apiUser = getUserFromToken(request);
        if (!apiUser.isValid()) {
            return new ResponseEntity<>(new ArrayList(), HttpStatus.FORBIDDEN);
        }

        return new ResponseEntity<>(positionList(apiUser.getUser()), HttpStatus.OK);
    }

    @GetMapping("/listdirectors/{id}")
    public ResponseEntity<List<User>> getDirectorList(@PathVariable BigInteger id, HttpServletRequest request) {
        APIUser apiUser = getUserFromToken(request);
        if (!apiUser.isValid()) {
            return new ResponseEntity<>(new ArrayList(), HttpStatus.FORBIDDEN);
        }
        Optional<User> user = userService.findById(id);
        if (user.isEmpty()) {
            return new ResponseEntity<>(new ArrayList(), HttpStatus.BAD_REQUEST);
        }

        String apiUserPermission = permissionType(apiUser.getUser());

        List<User> userListReturn = new ArrayList<>();
        System.out.println(apiUser.getUser().getDepartment().getName());
        Optional<DepartmentRegional> regionalDep = departmentRegionalService.findByName(apiUser.getUser().getDepartment().getName());
        Optional<DepartmentRegional> userDepartment = departmentRegionalService.findByName(user.get().getDepartment().getName());
        User director = user.get().getDirector();

        switch (permissionType(apiUser.getUser())) {
            case "Campus Department Users":
                userListReturn = userService.findAllByDepartmentCampus(apiUser.getUser().getDepartment());
                // make sure campus director is available
                Optional<User> campDir = userService.findById(apiUser.getUser().getCampus().getDirectorId());
                if (campDir.isPresent()) {
                    if (!userListReturn.contains(campDir.get())) {
                        userListReturn.add(campDir.get());
                    }
                }
                break;
            case "Campus Users":
                userListReturn = userService.findAllByCampus(apiUser.getUser().getCampus());
                // make sure users department director is available
                if (userDepartment.isPresent()) {
                    Optional<User> userDepartmentDir = userService.findById(userDepartment.get().getDirectorId());
                    if (userDepartmentDir.isPresent()) {
                        if (!userListReturn.contains(userDepartmentDir.get())) {
                            userListReturn.add(userDepartmentDir.get());
                        }
                    }
                }
                break;
            case "Department Users":
                if (regionalDep.isPresent()) {
                    userListReturn = userService.findAllByDepartment(regionalDep.get());
                }
                // make sure users campus director is available
                Optional<User> userCampusDir = userService.findById(user.get().getCampus().getDirectorId());
                if (userCampusDir.isPresent()) {
                    if (!userListReturn.contains(userCampusDir.get())) {
                        userListReturn.add(userCampusDir.get());
                    }
                }
                break;
            default:
                userListReturn = userService.findAll();
        }

        // make sure regional director is available
        if (regionalDep.isPresent()) {
            Optional<User> regDir = userService.findById(regionalDep.get().getDirectorId());
            if (regDir.isPresent()) {
                if (!userListReturn.contains(regDir.get())) {
                    userListReturn.add(regDir.get());
                }
            }
        }

        // make sure current user director is present
        if (!userListReturn.contains(user.get().getDirector())) {
            userListReturn.add(user.get().getDirector());
        }

        userListReturn.sort(new Comparator<User>() {
            @Override
            public int compare(User user1, User user2) {
                return user1.getFullName().compareToIgnoreCase(user2.getFullName());
            }
        });

        return new ResponseEntity<>(userListReturn, HttpStatus.OK);
    }

    @GetMapping("/listdirectors")
    public ResponseEntity<List<User>> getDirectorList(HttpServletRequest request) {
        APIUser apiUser = getUserFromToken(request);
        if (!apiUser.isValid()) {
            return new ResponseEntity<>(new ArrayList(), HttpStatus.FORBIDDEN);
        }

        String apiUserPermission = permissionType(apiUser.getUser());

        List<User> userListReturn = new ArrayList<>();
        System.out.println(apiUser.getUser().getDepartment().getName());
        Optional<DepartmentRegional> regionalDep = departmentRegionalService.findByName(apiUser.getUser().getDepartment().getName());

        switch (permissionType(apiUser.getUser())) {
            case "Campus Department Users":
                userListReturn = userService.findAllByDepartmentCampus(apiUser.getUser().getDepartment());
                break;
            case "Campus Users":
                userListReturn = userService.findAllByCampus(apiUser.getUser().getCampus());
                break;
            case "Department Users":
                if (regionalDep.isPresent()) {
                    userListReturn = userService.findAllByDepartment(regionalDep.get());
                }
                break;
            default:
                userListReturn = userService.findAll();
        }

        // make sure campus director is available
        Optional<User> campDir = userService.findById(apiUser.getUser().getCampus().getDirectorId());
        if (campDir.isPresent()) {
            if (!userListReturn.contains(campDir.get())) {
                userListReturn.add(campDir.get());
            }
        }

        // make sure regional director is available
        List<DepartmentRegional> regionals = departmentRegionalService.findAll();
        for (DepartmentRegional departmentRegional : regionals) {
            Optional<User> regDir = userService.findById(departmentRegional.getDirectorId());
            if (regDir.isPresent()) {
                if (!userListReturn.contains(regDir.get())) {
                    userListReturn.add(regDir.get());
                }
            }
        }

        userListReturn.sort(new Comparator<User>() {
            @Override
            public int compare(User user1, User user2) {
                return user1.getFullName().compareToIgnoreCase(user2.getFullName());
            }
        });

        return new ResponseEntity<>(userListReturn, HttpStatus.OK);
    }

    @PostMapping("/updateuser")
    public ResponseEntity<SingleBigIntegerModel> updateUser(@ModelAttribute UserModel userModel, HttpServletRequest request) {
        APIUser apiUser = getUserFromToken(request);
        if (!apiUser.isValid()) {
            return new ResponseEntity<>(new SingleBigIntegerModel(userModel.id()), HttpStatus.FORBIDDEN);
        }

        Optional<User> user = userService.findById(userModel.id());
        if (user.isEmpty()) {
            return new ResponseEntity<>(new SingleBigIntegerModel(userModel.id()), HttpStatus.BAD_REQUEST);
        }

        // strip phone number
        String phone = userModel.contactPhone().replaceAll("[^0-9]","");

        user.get().setFirstName(userModel.firstName());
        user.get().setLastName(userModel.lastName());
        user.get().setContactPhone(phone);
        user.get().setPosition(positionService.findByName(userModel.position()).orElse(null));
        user.get().setCampus(campusService.findByName(userModel.campus()).orElse(null));
        user.get().setDepartment(departmentCampusService.findByNameAndCampus(userModel.department(), user.get().getCampus()).orElse(null));
        user.get().setDirector(userService.findById(userModel.directorId()).orElse(null));
        userService.save(user.get());

        return new ResponseEntity<>(new SingleBigIntegerModel(userModel.id()), HttpStatus.OK);
    }

    @PostMapping("/newuser")
    public ResponseEntity<BigIntegerStringModel> newUser(@ModelAttribute UserModel userModel, HttpServletRequest request) {
        APIUser apiUser = getUserFromToken(request);
        if (!apiUser.isValid()) {
            return new ResponseEntity<>(new BigIntegerStringModel(userModel.id(), "Access Denied!"), HttpStatus.FORBIDDEN);
        }

        Optional<User> existingUser = userService.findByUsername(userModel.username());
        if (existingUser.isPresent()) {
            return new ResponseEntity<>(new BigIntegerStringModel(userModel.id(), "Username Already Exists!"), HttpStatus.BAD_REQUEST);
        }

        // strip phone number
        String phone = userModel.contactPhone().replaceAll("[^0-9]","");

        User user = new User();
        user.setUsername(userModel.username());
        user.setFirstName(userModel.username());
        user.setFirstName(userModel.firstName());
        user.setLastName(userModel.lastName());
        user.setContactEmail(userModel.username());
        user.setContactPhone(phone);
        user.setPosition(positionService.findByName(userModel.position()).orElse(null));
        user.setCampus(campusService.findByName(userModel.campus()).orElse(null));
        user.setDepartment(departmentCampusService.findByNameAndCampus(userModel.department(), user.getCampus()).orElse(null));
        user.setDirector(userService.findById(userModel.directorId()).orElse(null));

        // set permissions
        Optional<PermissionTemplate> template = permissionTemplateService.findFirstByPosition(user.getPosition());
        if (template.isPresent()) {
            for (UserRoles role : template.get().getUserRoles()) {
                user.getUserRoles().add(role);
            }
        }

        // save the new user
        user = userService.createUser(user);

        // Send Password E-mail
        AdminSettings adminSettings = adminService.getAdminSettings();
        EmailDetails emailDetails = new EmailDetails(user.getContactEmail(),
                "Welcome to " + adminSettings.getCompanyName(),
                "Welcome to " + adminSettings.getCompanyName(), null);
        BrowserMessage msg = emailService.sendWelcomeMail(emailDetails, user.getId());

        // send any additional notifications
        AppEvent notifyEvent = eventService.createEvent(new AppEvent(
                null,
                "New User "+user.getFullName()+" has been added",
                "User "+user.getFullName()+" was added by "+apiUser.getUser().getFullName(),
                user.getId().toString(),
                EventModule.User,
                EventType.New,
                new ArrayList<>()
        ));

        return new ResponseEntity<>(new BigIntegerStringModel(user.getId(), "Success!"), HttpStatus.OK);
    }

    private APIUser getUserFromToken(HttpServletRequest request) {
        final String authHeader = request.getHeader("Authorization");
        final String token;
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);
            try {
                Optional<User> currentUser = userService.findByUsername(jwtService.extractUsername(token));
                if (currentUser.isPresent()) {
                    if (jwtService.isTokenValid(token, currentUser.get())) {
                        return new APIUser(true, currentUser.get());
                    }
                }
            } catch (ExpiredJwtException e) {
                System.out.println("Token Expired");
            }
        }
        return new APIUser(false, new User());
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
}
