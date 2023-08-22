package net.dahliasolutions.controllers.support;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import net.dahliasolutions.models.campus.Campus;
import net.dahliasolutions.models.department.DepartmentCampus;
import net.dahliasolutions.models.department.DepartmentRegional;
import net.dahliasolutions.models.position.Position;
import net.dahliasolutions.models.records.BigIntegerStringModel;
import net.dahliasolutions.models.support.Ticket;
import net.dahliasolutions.models.support.TicketModel;
import net.dahliasolutions.models.support.TicketNewModel;
import net.dahliasolutions.models.support.TicketStatus;
import net.dahliasolutions.models.user.User;
import net.dahliasolutions.models.user.UserRoles;
import net.dahliasolutions.services.EventService;
import net.dahliasolutions.services.RedirectService;
import net.dahliasolutions.services.campus.CampusService;
import net.dahliasolutions.services.department.DepartmentCampusService;
import net.dahliasolutions.services.department.DepartmentRegionalService;
import net.dahliasolutions.services.mail.EmailService;
import net.dahliasolutions.services.support.TicketPriorityService;
import net.dahliasolutions.services.user.UserRolesService;
import net.dahliasolutions.services.user.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigInteger;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;

@Controller
@RequestMapping("/support")
@RequiredArgsConstructor
public class SupportController {

    private final TicketPriorityService ticketPriorityService;
    private final UserService userService;
    private final UserRolesService rolesService;
    private final CampusService campusService;
    private final DepartmentRegionalService departmentRegionalService;
    private final DepartmentCampusService departmentCampusService;
    private final RedirectService redirectService;
    private final EmailService emailService;
    private final EventService eventService;

    @ModelAttribute
    public void addAttributes(Model model) {
        model.addAttribute("moduleTitle", "Support");
        model.addAttribute("moduleLink", "/support");
    }
    @GetMapping("")
    public String goSupportHome(Model model, HttpSession session) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) auth.getPrincipal();

        List<Ticket> myTickets = new ArrayList<>();
        List<Ticket> agentTickets = new ArrayList<>();
        List<Ticket> supervisorTickets = new ArrayList<>();
        List<Ticket> campusTickets = new ArrayList<>();
        List<Ticket> departmentTickets = new ArrayList<>();


        model.addAttribute("myTickets", myTickets);
        model.addAttribute("agentTickets", agentTickets);
        model.addAttribute("supervisorTickets", supervisorTickets);
        model.addAttribute("campusTickets", campusTickets);
        model.addAttribute("departmentTickets", departmentTickets);

        redirectService.setHistory(session, "/support");
        return "support/index";
    }

    @GetMapping("/ticket/{id}")
    public String goTicket(@PathVariable BigInteger id, Model model, HttpSession session) {

        redirectService.setHistory(session, "/support/ticket"+id);
        return "support/ticket";
    }

    @GetMapping("/settings")
    public String goSupportSettings(Model model) {

        model.addAttribute("priorityList", ticketPriorityService.findAll());
        return "support/settings";
    }

    @GetMapping("/new")
    public String goNewTicket(Model model, HttpSession session) {
        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        model.addAttribute("priorityList", ticketPriorityService.findAll());
        model.addAttribute("campusList", campusList(currentUser));
        model.addAttribute("departmentList", departmentList(currentUser));

        redirectService.setHistory(session, "/support");
        return "support/ticketNew";
    }

    @PostMapping("/create")
    public String createTicket(@ModelAttribute TicketNewModel ticketNewModel, HttpSession session) {
        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        Ticket ticket = new Ticket(
                null,
                LocalDateTime.now(),
                LocalDateTime.now(),
                ticketNewModel.getSummary(),
                ticketNewModel.getPriority(),
                null,
                TicketStatus.Open,
                campusService.findById(ticketNewModel.getCampus()).get(),
                departmentRegionalService.findById(ticketNewModel.getDepartment()).get(),
                currentUser,
                currentUser.getDirector(),
                new ArrayList<>(),
                new ArrayList<>());

        return "redirect:/ticket/"+ BigInteger.valueOf(1).toString();
    }

    @GetMapping("/search/{searchTerm}")
    public String searchTickets(@PathVariable String searchTerm, Model model, HttpSession session) {
        redirectService.setHistory(session, "/support/search/title/"+searchTerm);
        String searcher = URLDecoder.decode(searchTerm, StandardCharsets.UTF_8);
        // List<StoreItem> itemList = storeItemService.searchAll(searcher);

        // model.addAttribute("storeItems", itemList);
        return "support/index";
    }


    // get edit permission
    private boolean supportEditor(User currentUser){
        Collection<UserRoles> roles = currentUser.getUserRoles();
        for (UserRoles role : roles){
            if (role.getName().equals("ADMIN_WRITE") || role.getName().equals("SUPPORT_AGENT")
                    || role.getName().equals("SUPPORT_SUPERVISOR")) {
                return true;
            }
        }
        return false;
    }

    private String permissionType(User currentUser) {
        Collection<UserRoles> roles = currentUser.getUserRoles();
        String typeString = "Campus Users";
        for (UserRoles role : roles){
            if (role.getName().equals("ADMIN_WRITE") || role.getName().equals("SUPPORT_SUPERVISOR")) {
                typeString = "Admin";
            } else if (role.getName().equals("DIRECTOR_WRITE") || role.getName().equals("DIRECTOR_READ")) {
                typeString = "Department";
            } else if (role.getName().equals("CAMPUS_WRITE") || role.getName().equals("CAMPUS_READ")) {
                typeString = "Campus";
            } else if (role.getName().equals("SUPPORT_AGENT")) {
                typeString = "Assigned";
            }
        }
        return typeString;
    }

    private List<Campus> campusList(User currentUser) {
        List<Campus> campusList = new ArrayList<>();
        Collection<UserRoles> roles = currentUser.getUserRoles();
        for (UserRoles role : roles){
            if (role.getName().equals("ADMIN_WRITE") || role.getName().equals("SUPPORT_SUPERVISOR")
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
            if (role.getName().equals("ADMIN_WRITE") || role.getName().equals("SUPPORT_SUPERVISOR")
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

}
