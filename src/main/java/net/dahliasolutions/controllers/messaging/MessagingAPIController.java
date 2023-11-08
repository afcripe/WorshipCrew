package net.dahliasolutions.controllers.messaging;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import net.dahliasolutions.models.EventModule;
import net.dahliasolutions.models.EventType;
import net.dahliasolutions.models.NotificationMessage;
import net.dahliasolutions.models.NotificationMessageModel;
import net.dahliasolutions.models.campus.Campus;
import net.dahliasolutions.models.department.DepartmentCampus;
import net.dahliasolutions.models.department.DepartmentRegional;
import net.dahliasolutions.models.mail.MailerCustomMessage;
import net.dahliasolutions.models.mail.MailerCustomMessageModel;
import net.dahliasolutions.models.mail.MessageGroupEnum;
import net.dahliasolutions.models.position.Position;
import net.dahliasolutions.models.records.SingleBigIntegerModel;
import net.dahliasolutions.models.records.UserListFilterModel;
import net.dahliasolutions.models.user.User;
import net.dahliasolutions.models.user.UserRoles;
import net.dahliasolutions.models.user.UserSelectedModel;
import net.dahliasolutions.services.campus.CampusService;
import net.dahliasolutions.services.department.DepartmentCampusService;
import net.dahliasolutions.services.department.DepartmentRegionalService;
import net.dahliasolutions.services.mail.MailerCustomMessageService;
import net.dahliasolutions.services.mail.NotificationMessageService;
import net.dahliasolutions.services.user.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.math.BigInteger;
import java.util.*;

@RestController
@RequestMapping("/api/v1/messaging")
@RequiredArgsConstructor
public class MessagingAPIController {

    private final MailerCustomMessageService mailerCustomMessageService;
    private final NotificationMessageService notificationMessageService;
    private final CampusService campusService;
    private final DepartmentRegionalService departmentRegionalService;
    private final DepartmentCampusService departmentCampusService;
    private final UserService userService;

    @PostMapping("/save")
    public SingleBigIntegerModel saveDraft(@ModelAttribute MailerCustomMessageModel messageModel) {
        // convert model to entity
        MailerCustomMessage message = mailerCustomMessageService.convertModelToEntity(messageModel);
        // save message
        return new SingleBigIntegerModel(mailerCustomMessageService.save(message).getId());
    }

    @PostMapping("/delete")
    public SingleBigIntegerModel deleteDraft(@ModelAttribute SingleBigIntegerModel messageModel) {
        mailerCustomMessageService.deleteById(messageModel.id());
        return messageModel;
    }

    @PostMapping("/send")
    public SingleBigIntegerModel sendMessage(@ModelAttribute MailerCustomMessageModel messageModel) {
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
        mailerCustomMessageService.save(message);

        // return id
        return new SingleBigIntegerModel(mailerCustomMessageService.save(message).getId());
    }

    @GetMapping("/message/{id}")
    public NotificationMessageModel getMessageById(@PathVariable BigInteger id) {
        Optional<NotificationMessage> notification = notificationMessageService.findById(id);
        NotificationMessageModel message = new NotificationMessageModel();
        // set from name
        String fromName = "System";
        Optional<User> fromUser = userService.findById(notification.get().getFromUserId());
        if (fromUser.isPresent()) {
            fromName = fromUser.get().getFullName();
        }
        if (notification.isPresent()) {
            message.setId(notification.get().getId());
            message.setSubject(notification.get().getSubject());
            message.setDateSent(notification.get().getDateSent());
            message.setRead(notification.get().isRead());
            message.setModule(notification.get().getModule().toString());
            message.setFromUser(fromName);
        }
        return message;
    }

    @GetMapping("/readstate/read/{id}")
    public int setMessageReadById(@PathVariable BigInteger id) {
        Optional<NotificationMessage> notification = notificationMessageService.findById(id);
        if (notification.isPresent()) {
            notification.get().setRead(true);
            notificationMessageService.save(notification.get());
            return 1;
        }
        return 0;
    }

    @GetMapping("/readstate/unread/{id}")
    public int setMessageUnreadById(@PathVariable BigInteger id) {
        Optional<NotificationMessage> notification = notificationMessageService.findById(id);
        if (notification.isPresent()) {
            notification.get().setRead(false);
            notificationMessageService.save(notification.get());
            return 1;
        }
        return 0;
    }

    @GetMapping("/campuslist")
    public List<String> getMessageListCampus() {
        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        List<String> returnList = new ArrayList<>();

        List<Campus> campuses = campusList(currentUser);
        if (campuses.size()>1) { returnList.add("All");}

        for (Campus campus : campuses) {
            returnList.add(campus.getName());
        }
        return returnList;
    }

    @GetMapping("/departmentlist")
    public List<String> getMessageListDepartment() {
        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        List<String> returnList = new ArrayList<>();

        List<DepartmentRegional> departments = departmentList(currentUser);
        if (departments.size()>1) { returnList.add("All");}

        for (DepartmentRegional department : departments) {
            returnList.add(department.getName());
        }
        return returnList;
    }

    @PostMapping("/userlist/{id}")
    public List<UserSelectedModel> getMessageListUsers(@PathVariable BigInteger id, @ModelAttribute UserListFilterModel listFilter) {
        System.out.println(listFilter);
        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

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
                users = userList(currentUser);
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
        return selectUsers;
    }

    // get edit permission
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
