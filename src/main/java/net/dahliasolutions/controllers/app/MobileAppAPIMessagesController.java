package net.dahliasolutions.controllers.app;

import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import net.dahliasolutions.models.*;
import net.dahliasolutions.models.campus.Campus;
import net.dahliasolutions.models.department.DepartmentCampus;
import net.dahliasolutions.models.department.DepartmentRegional;
import net.dahliasolutions.models.mail.MailerCustomMessage;
import net.dahliasolutions.models.mail.MailerCustomMessageModel;
import net.dahliasolutions.models.order.OrderItem;
import net.dahliasolutions.models.order.OrderNote;
import net.dahliasolutions.models.order.OrderRequest;
import net.dahliasolutions.models.records.*;
import net.dahliasolutions.models.support.*;
import net.dahliasolutions.models.user.User;
import net.dahliasolutions.models.user.UserRoles;
import net.dahliasolutions.models.user.UserSelectedModel;
import net.dahliasolutions.services.JwtService;
import net.dahliasolutions.services.campus.CampusService;
import net.dahliasolutions.services.department.DepartmentCampusService;
import net.dahliasolutions.services.department.DepartmentRegionalService;
import net.dahliasolutions.services.mail.MailerCustomMessageService;
import net.dahliasolutions.services.mail.NotificationMessageService;
import net.dahliasolutions.services.support.*;
import net.dahliasolutions.services.user.UserRolesService;
import net.dahliasolutions.services.user.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/app/messages")
public class MobileAppAPIMessagesController {

    private final JwtService jwtService;
    private final UserService userService;
    private final MailerCustomMessageService mailerCustomMessageService;
    private final NotificationMessageService notificationMessageService;
    private final UserRolesService rolesService;
    private final CampusService campusService;
    private final DepartmentRegionalService departmentRegionalService;
    private final DepartmentCampusService departmentCampusService;
    private final AppServer appServer;

    @GetMapping("/listnew")
    public ResponseEntity<List<NotificationMessageModel>> getTicketsByUser(
            @RequestParam Optional<String> system, @RequestParam Optional<String> read,
            @RequestParam Optional<String> draft, HttpServletRequest request) {

        APIUser apiUser = getUserFromToken(request);
        if (!apiUser.isValid()) {
            return new ResponseEntity<>(new ArrayList<>(), HttpStatus.FORBIDDEN);
        }

        // set default messages to display
        boolean showSystemMsg = false;
        boolean showAllMsg = false;
        boolean showDraft = false;

        if (system.isPresent()) {
            if (!system.get().toLowerCase().equals("false")) {
                showSystemMsg = true;
            }
        }
        if (read.isPresent()) {
            if (!read.get().toLowerCase().equals("false")) {
                showAllMsg = true;
            }
        }
        if (draft.isPresent()) {
            if (!draft.get().toLowerCase().equals("false")) {
                showDraft = true;
            }
        }

        // get user messages
        List<NotificationMessage> notificationMessageList = notificationMessageService.getUserAll(apiUser.getUser());

        // convert to message model
        List<NotificationMessageModel> modelList = new ArrayList<>();
        if (!showDraft) {
            for (NotificationMessage message : notificationMessageList) {
                // set from name
                Optional<User> fromUser = userService.findById(message.getFromUserId());
                String sendingUser = "System Message";
                if (fromUser.isPresent()) {
                    sendingUser = fromUser.get().getFullName();
                }

                if (message.getFromUserId().equals(BigInteger.valueOf(0))) {
                    if (showSystemMsg) {
                        if (showAllMsg) {
                            NotificationMessageModel messageModel = new NotificationMessageModel(
                                    message.getId(),
                                    message.getSubject(),
                                    message.getDateSent(),
                                    message.isRead(),
                                    sendingUser,
                                    message.getModule().toString(),
                                    ""
                            );
                            modelList.add(messageModel);
                        } else if (!message.isRead()) {
                            NotificationMessageModel messageModel = new NotificationMessageModel(
                                    message.getId(),
                                    message.getSubject(),
                                    message.getDateSent(),
                                    message.isRead(),
                                    sendingUser,
                                    message.getModule().toString(),
                                    ""
                            );
                            modelList.add(messageModel);
                        }
                    }
                } else {
                    if (showAllMsg) {
                        NotificationMessageModel messageModel = new NotificationMessageModel(
                                message.getId(),
                                message.getSubject(),
                                message.getDateSent(),
                                message.isRead(),
                                sendingUser,
                                message.getModule().toString(),
                                ""
                        );
                        modelList.add(messageModel);
                    } else if (!message.isRead()) {
                        NotificationMessageModel messageModel = new NotificationMessageModel(
                                message.getId(),
                                message.getSubject(),
                                message.getDateSent(),
                                message.isRead(),
                                sendingUser,
                                message.getModule().toString(),
                                ""
                        );
                        modelList.add(messageModel);
                    }
                }

            }
        } else {
            List<MailerCustomMessage> customMessages = mailerCustomMessageService.findByUserAndDraft(apiUser.getUser(), true);
            for (MailerCustomMessage msg : customMessages) {
                NotificationMessageModel messageModel = new NotificationMessageModel(
                        msg.getId(),
                        msg.getSubject(),
                        null,
                        false,
                        "(Draft)",
                        EventModule.Messaging.toString(),
                        ""
                );
                modelList.add(messageModel);
            }
        }

        // sort by date and revers to put new on top
        modelList.sort(new Comparator<NotificationMessageModel>() {
            @Override
            public int compare(NotificationMessageModel msg1, NotificationMessageModel msg2) {
                int i;
                try {
                    i = msg1.getDateSent().compareTo(msg2.getDateSent());
                } catch (Exception e) {
                    i = 1;
                }
                return i;
            }
        });
        Collections.reverse(modelList);

        return new ResponseEntity<>(modelList, HttpStatus.OK);
    }

    @GetMapping("/message/{id}")
    public ResponseEntity<NotificationMessageModel> getMessageBodyContent(@PathVariable BigInteger id, HttpServletRequest request) {

        APIUser apiUser = getUserFromToken(request);
        if (!apiUser.isValid()) {
            return new ResponseEntity<>(new NotificationMessageModel(), HttpStatus.FORBIDDEN);
        }

        // get message
        Optional<NotificationMessage> message = notificationMessageService.findById(id);
        NotificationMessageModel messageModel = new NotificationMessageModel();

        if (message.isPresent()) {
            Optional<User> fromUser = userService.findById(message.get().getFromUserId());
            String sendingUser = "System Message";
            if (fromUser.isPresent()) {
                sendingUser = fromUser.get().getFullName();
            }

            messageModel = new NotificationMessageModel(
                    message.get().getId(),
                    message.get().getSubject(),
                    message.get().getDateSent(),
                    message.get().isRead(),
                    sendingUser,
                    message.get().getModule().toString(),
                    ""
            );
        }
        return new ResponseEntity<>(messageModel, HttpStatus.OK);

    }


    @GetMapping("/draft/{id}")
    public ResponseEntity<MailerCustomMessageModel> getDraftContent(@PathVariable BigInteger id, HttpServletRequest request) {

        APIUser apiUser = getUserFromToken(request);
        if (!apiUser.isValid()) {
            return new ResponseEntity<>(new MailerCustomMessageModel(), HttpStatus.FORBIDDEN);
        }

        MailerCustomMessageModel messageModel;
        Optional<MailerCustomMessage> message = mailerCustomMessageService.findById(id);
        if (message.isPresent()) {
            messageModel = mailerCustomMessageService.convertEntityToModel(message.get());
        } else {
            messageModel = new MailerCustomMessageModel(BigInteger.valueOf(0), "", true, "", apiUser.getUser().getId(), "", "");
        }
        return new ResponseEntity<>(messageModel, HttpStatus.OK);
    }


    @GetMapping("/readstate/read/{id}")
    public ResponseEntity<SingleIntModel> setMessageReadById(@PathVariable BigInteger id, HttpServletRequest request) {

        APIUser apiUser = getUserFromToken(request);
        if (!apiUser.isValid()) {
            return new ResponseEntity<>(new SingleIntModel(0), HttpStatus.FORBIDDEN);
        }

        Optional<NotificationMessage> notification = notificationMessageService.findById(id);
        if (notification.isPresent()) {
            notification.get().setRead(true);
            notificationMessageService.save(notification.get());
            return new ResponseEntity<>(new SingleIntModel(1), HttpStatus.OK);
        }
        return new ResponseEntity<>(new SingleIntModel(0), HttpStatus.OK);
    }

    @GetMapping("/readstate/unread/{id}")
    public ResponseEntity<SingleIntModel> setMessageUnreadById(@PathVariable BigInteger id, HttpServletRequest request) {

        APIUser apiUser = getUserFromToken(request);
        if (!apiUser.isValid()) {
            return new ResponseEntity<>(new SingleIntModel(0), HttpStatus.FORBIDDEN);
        }

        Optional<NotificationMessage> notification = notificationMessageService.findById(id);
        if (notification.isPresent()) {
            notification.get().setRead(false);
            notificationMessageService.save(notification.get());
            return new ResponseEntity<>(new SingleIntModel(1), HttpStatus.OK);
        }
        return new ResponseEntity<>(new SingleIntModel(0), HttpStatus.OK);
    }

    @GetMapping("/grouplist")
    public ResponseEntity<List<String>> getGrouList(HttpServletRequest request) {
        APIUser apiUser = getUserFromToken(request);
        if (!apiUser.isValid()) {
            return new ResponseEntity<>(new ArrayList<>(), HttpStatus.FORBIDDEN);
        }
        return new ResponseEntity<>(permissionList(apiUser.getUser()), HttpStatus.OK);
    }

    @GetMapping("/campuslist")
    public ResponseEntity<List<String>> getMessageListCampus(HttpServletRequest request) {
        APIUser apiUser = getUserFromToken(request);
        if (!apiUser.isValid()) {
            return new ResponseEntity<>(new ArrayList<>(), HttpStatus.FORBIDDEN);
        }

        List<String> returnList = new ArrayList<>();

        List<Campus> campuses = campusList(apiUser.getUser());
        if (campuses.size()>1) { returnList.add("All");}

        for (Campus campus : campuses) {
            returnList.add(campus.getName());
        }
        return new ResponseEntity<>(returnList, HttpStatus.OK);
    }

    @GetMapping("/departmentlist")
    public ResponseEntity<List<String>> getMessageListDepartment(HttpServletRequest request) {
        APIUser apiUser = getUserFromToken(request);
        if (!apiUser.isValid()) {
            return new ResponseEntity<>(new ArrayList<>(), HttpStatus.FORBIDDEN);
        }
        List<String> returnList = new ArrayList<>();

        List<DepartmentRegional> departments = departmentList(apiUser.getUser());
        if (departments.size()>1) { returnList.add("All");}

        for (DepartmentRegional department : departments) {
            returnList.add(department.getName());
        }
        return new ResponseEntity<>(returnList, HttpStatus.OK);
    }

    @PostMapping("/userlist/{id}")
    public ResponseEntity<List<UserSelectedModel>> getUserList(@PathVariable BigInteger id, @ModelAttribute UserListFilterModel listFilter,HttpServletRequest request) {
        APIUser apiUser = getUserFromToken(request);
        if (!apiUser.isValid()) {
            return new ResponseEntity<>(new ArrayList<>(), HttpStatus.FORBIDDEN);
        }

        List<User> currentToUsers = new ArrayList<>();
        Optional<MailerCustomMessage> message = mailerCustomMessageService.findById(id);
        if (message.isPresent()) {
            currentToUsers = message.get().getToUsers();
        }

        List<Campus> campusList = new ArrayList<>();
        List<DepartmentRegional> departmentList = new ArrayList<>();
        List<User> users = new ArrayList<>();
        // get requested campuses
        if (!listFilter.campus().equals("")) {
            List<String> cItems = Arrays.asList(listFilter.campus().split("\s"));
            if (cItems.contains("All")) {
                campusList = campusService.findAll();
            } else {
                for (String s : cItems) {
                    if (!s.equals("")) {
                        try {
                            Optional<Campus> c = campusService.findByName(s);
                            if (c.isPresent()) {
                                campusList.add(c.get());
                            }
                        } catch (Error e) {
                            System.out.println(e);
                        }
                    }
                }
            }
        }
        // get requested departments
        if (!listFilter.department().equals("")) {
            List<String> dItems = Arrays.asList(listFilter.department().split("\s"));
            if (dItems.contains("All")) {
                departmentList = departmentRegionalService.findAll();
            } else {
                for (String s : dItems) {
                    if (!s.equals("")) {
                        try {
                            Optional<DepartmentRegional> d = departmentRegionalService.findByName(s);
                            if (d.isPresent()) {
                                departmentList.add(d.get());
                            }
                        } catch (Error e) {
                            System.out.println(e);
                        }
                    }
                }
            }
        }

        List<User> cUsers = new ArrayList<>();
        List<User> dUsers = new ArrayList<>();

        switch (listFilter.listType()) {
            case "Campus Users":
                for (Campus campus : campusList) {
                    // Select all for campuses
                    cUsers = userService.findAllByCampus(campus);
                    for (User user : cUsers) {
                        if (!dUsers.contains(user)) { dUsers.add(user);}
                    }
                    // filter for departments
                    for (User user : dUsers) {
                        if (!users.contains(user)
                                && departmentList.contains(user.getDepartment().getRegionalDepartment())) {
                            users.add(user);
                        }
                    }
                }
                break;
            case "Department Users":
                for (DepartmentRegional department : departmentList) {
                    // Select all for department
                    dUsers = userService.findAllByDepartment(department);
                    for (User user : dUsers) {
                        if (!cUsers.contains(user)) { cUsers.add(user);}
                    }
                    // filter for campus
                    for (User user : cUsers) {
                        if (!users.contains(user)
                                && campusList.contains(user.getCampus())) {
                            users.add(user);
                        }
                    }
                }
                break;
            case "Campus Department Directors":
                for (Campus campus : campusList) {
                    // Select departments for campuses
                    List<DepartmentCampus> campusDepartments = departmentCampusService.findAllByCampus(campus);
                    for (DepartmentCampus dc : campusDepartments) {
                        if (departmentList.contains(dc.getRegionalDepartment())) {
                            Optional<User> dir = userService.findById(dc.getRegionalDepartment().getDirectorId());
                            if (dir.isPresent()) {
                                if (!users.contains(dir.get())) { users.add(dir.get());}
                            }

                        }
                    }
                }
                break;
            case "Campus Directors":
                for (Campus campus : campusList) {
                    // Select directors for campuses
                    Optional<User> dir = userService.findById(campus.getDirectorId());
                    if (dir.isPresent()) {
                        if (!users.contains(dir.get())) {users.add(dir.get());}
                    }
                }
                break;
            case "Regional Department Directors":
                for (DepartmentRegional department : departmentList) {
                    // Select directors for department
                    Optional<User> dir = userService.findById(department.getDirectorId());
                    if (dir.isPresent()) {
                        if (!users.contains(dir.get())) {users.add(dir.get());}
                    }
                }
                break;
            case "All Users":
                users = userList(apiUser.getUser());
        }

        List<UserSelectedModel> selectUsers = new ArrayList<>();
        if (users.size()>1) {
            selectUsers.add(new UserSelectedModel(BigInteger.valueOf(0), "All", false));
        }
        for (User user : users) {
            UserSelectedModel tempUser = new UserSelectedModel(user.getId(), user.getFullName(), false);
            if (currentToUsers.contains(user)) {
                tempUser.setSelected(true);
            }
            selectUsers.add(tempUser);
        }
        return new ResponseEntity<>(selectUsers, HttpStatus.OK);
    }


    @PostMapping("/save")
    public ResponseEntity<SingleBigIntegerModel> saveDraft(@ModelAttribute MailerCustomMessageModel messageModel, HttpServletRequest request) {
        APIUser apiUser = getUserFromToken(request);
        if (!apiUser.isValid()) {
            return new ResponseEntity<>(new SingleBigIntegerModel(BigInteger.valueOf(0)), HttpStatus.FORBIDDEN);
        }
        // convert model to entity
        MailerCustomMessage message = mailerCustomMessageService.convertModelToEntity(messageModel);
        // save message
        return new ResponseEntity<>(new SingleBigIntegerModel(mailerCustomMessageService.save(message).getId()), HttpStatus.OK);
    }

    @PostMapping("/delete")
    public ResponseEntity<SingleBigIntegerModel> deleteDraft(@ModelAttribute SingleBigIntegerModel messageModel, HttpServletRequest request) {
        APIUser apiUser = getUserFromToken(request);
        if (!apiUser.isValid()) {
            return new ResponseEntity<>(new SingleBigIntegerModel(BigInteger.valueOf(0)), HttpStatus.FORBIDDEN);
        }
        mailerCustomMessageService.deleteById(messageModel.id());
        return new ResponseEntity<>(messageModel, HttpStatus.OK);
    }

    @PostMapping("/send")
    public ResponseEntity<SingleBigIntegerModel> sendMessage(@ModelAttribute MailerCustomMessageModel messageModel, HttpServletRequest request) {
        APIUser apiUser = getUserFromToken(request);
        if (!apiUser.isValid()) {
            return new ResponseEntity<>(new SingleBigIntegerModel(BigInteger.valueOf(0)), HttpStatus.FORBIDDEN);
        }
        // convert model to entity and save
        MailerCustomMessage message = mailerCustomMessageService.convertModelToEntity(messageModel);
        message = mailerCustomMessageService.save(message);

        // loop over to users and send message
        for (User user : message.getToUsers()) {
            NotificationMessage returnMsg = notificationMessageService.createMessage(
                    new NotificationMessage(
                            null,
                            message.getSubject(),
                            "0",
                            BigInteger.valueOf(0),
                            null,
                            false,
                            false,
                            null,
                            false,
                            message.getUser().getId(),
                            EventModule.Messaging,
                            EventType.Custom,
                            user,
                            message.getId()
                    ));
        }

        // remove draft designation
        message.setDraft(false);

        // return id
        return new ResponseEntity<>(new SingleBigIntegerModel(mailerCustomMessageService.save(message).getId()), HttpStatus.OK);
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

    private List<String> permissionList(User currentUser) {
        Collection<UserRoles> roles = currentUser.getUserRoles();

        List<String> permList = new ArrayList<>();
        permList.add("All Users");

        for (UserRoles role : roles) {
            if (role.getName().equals("CAMPUS_WRITE") || role.getName().equals("CAMPUS_READ")
                    || role.getName().equals("ADMIN_WRITE") || role.getName().equals("USER_SUPERVISOR")) {
                if (!permList.contains("Campus Users")) { permList.add("Campus Users"); }
                if (!permList.contains("Campus Department Directors")) { permList.add("Campus Department Directors"); }
            }
            if (role.getName().equals("DIRECTOR_WRITE") || role.getName().equals("DIRECTOR_READ")
                    || role.getName().equals("ADMIN_WRITE") || role.getName().equals("USER_SUPERVISOR")) {
                if (!permList.contains("Department Users")) { permList.add("Department Users"); }
                if (!permList.contains("Campus Department Directors")) {permList.add("Campus Department Directors");}
            }
            if (role.getName().equals("ADMIN_WRITE") || role.getName().equals("USER_SUPERVISOR")) {
                if (!permList.contains("Campus Directors")) { permList.add("Campus Directors"); }
                if (!permList.contains("Campus Directors")) { permList.add("Campus Directors"); }
            }

            /*

            if (role.getName().equals("USER_WRITE") || role.getName().equals("USER_READ")
                    || role.getName().equals("ADMIN_WRITE") || role.getName().equals("USER_SUPERVISOR")) {
                permList.add("NA");
            }

                select Individuals

                campus department users

                campus
                    campus users
                    campus department users
                    campus department Directors

                department
                    department users
                    department campus users
                    department campus Directors

                campus leads
                department lead
                all users

             */

        }
        return permList;
    }
    private List<Campus> campusList(User currentUser) {
        Collection<UserRoles> roles = currentUser.getUserRoles();
        List<Campus> campuses = new ArrayList<>();
        int priority = 0;
        for (UserRoles role : roles) {
            if (role.getName().equals("USER_WRITE") || role.getName().equals("USER_READ")
                    || role.getName().equals("CAMPUS_WRITE") || role.getName().equals("CAMPUS_READ")) {
                if (priority < 2) {
                    campuses.add(currentUser.getCampus());
                    priority = 2;
                }
            } else if (role.getName().equals("DIRECTOR_WRITE") || role.getName().equals("DIRECTOR_READ")
                    || role.getName().equals("ADMIN_WRITE") || role.getName().equals("USER_SUPERVISOR")) {
                if (priority < 4) {
                    campuses = campusService.findAll();
                    priority = 4;
                }
            }
        }
        return campuses;
    }
    private List<DepartmentRegional> departmentList(User currentUser) {
        Collection<UserRoles> roles = currentUser.getUserRoles();
        List<DepartmentRegional> departments = new ArrayList<>();
        int priority = 0;
        for (UserRoles role : roles) {
            if (role.getName().equals("USER_WRITE") || role.getName().equals("USER_READ")
                    || role.getName().equals("DIRECTOR_WRITE") || role.getName().equals("DIRECTOR_READ")) {
                if (priority < 2) {
                    departments.add(currentUser.getDepartment().getRegionalDepartment());
                    priority = 2;
                }
            } else if (role.getName().equals("CAMPUS_WRITE") || role.getName().equals("CAMPUS_READ")
                    || role.getName().equals("ADMIN_WRITE") || role.getName().equals("USER_SUPERVISOR")) {
                if (priority < 4) {
                    departments = departmentRegionalService.findAll();
                    priority = 4;
                }
            }
        }
        return departments;
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

}
