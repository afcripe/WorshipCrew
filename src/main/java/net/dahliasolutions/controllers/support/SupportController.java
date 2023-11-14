package net.dahliasolutions.controllers.support;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import net.dahliasolutions.models.*;
import net.dahliasolutions.models.campus.Campus;
import net.dahliasolutions.models.department.DepartmentCampus;
import net.dahliasolutions.models.department.DepartmentRegional;
import net.dahliasolutions.models.mail.EmailDetails;
import net.dahliasolutions.models.order.OrderItemDepartment;
import net.dahliasolutions.models.order.OrderRequest;
import net.dahliasolutions.models.order.OrderRequestCampus;
import net.dahliasolutions.models.order.OrderStatus;
import net.dahliasolutions.models.support.*;
import net.dahliasolutions.models.user.User;
import net.dahliasolutions.models.user.UserRoles;
import net.dahliasolutions.services.EventService;
import net.dahliasolutions.services.MessageService;
import net.dahliasolutions.services.RedirectService;
import net.dahliasolutions.services.campus.CampusService;
import net.dahliasolutions.services.department.DepartmentCampusService;
import net.dahliasolutions.services.department.DepartmentRegionalService;
import net.dahliasolutions.services.mail.EmailService;
import net.dahliasolutions.services.mail.NotificationMessageService;
import net.dahliasolutions.services.support.*;
import net.dahliasolutions.services.user.UserRolesService;
import net.dahliasolutions.services.user.UserService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static java.lang.Math.abs;

@Controller
@RequestMapping("/support")
@RequiredArgsConstructor
public class SupportController {

    private final TicketService ticketService;
    private final TicketPriorityService ticketPriorityService;
    private final UserService userService;
    private final UserRolesService rolesService;
    private final CampusService campusService;
    private final DepartmentRegionalService departmentRegionalService;
    private final DepartmentCampusService departmentCampusService;
    private final SupportSettingService supportSettingService;
    private final TicketSLAService slaService;
    private final TicketImageService ticketImageService;
    private final RedirectService redirectService;
    private final EmailService emailService;
    private final NotificationMessageService messageService;
    private final EventService eventService;

    @ModelAttribute
    public void addAttributes(Model model) {
        model.addAttribute("moduleTitle", "Support");
        model.addAttribute("moduleLink", "/support");
    }

    @GetMapping("")
    public String goSupportHome(Model model, HttpSession session) {
        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        List<Ticket> openUserTicketList = ticketService.findAllByUserOpenOnly(currentUser);
        List<Ticket> openAgentTicketList = ticketService.findAllByAgentOpenOnly(currentUser);
        List<Ticket> ticketMentionList = ticketService.findAllByMentionOpenOnly(currentUser);

        model.addAttribute("user", currentUser);
        model.addAttribute("openUserTicketList", openUserTicketList);
        model.addAttribute("openAgentTicketList", openAgentTicketList);
        model.addAttribute("ticketMentionList", ticketMentionList);


        redirectService.setHistory(session, "/support");
        return "support/ticketList";
    }

    @GetMapping("/openticketmanager")
    public String goAllOpenTickets(Model model, HttpSession session) {
        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        String perm = permissionType(currentUser);
        if (!Objects.equals(perm, "Admin")) {
            model.addAttribute("msgError", "Access Denied!");
            return redirectService.pathName(session, "/support");
        }

        List<Ticket> ticketList = ticketService.findAllOpen();

        model.addAttribute("user", currentUser);
        model.addAttribute("ticketList", ticketList);

        redirectService.setHistory(session, "/support/openticketmanager");
        return "support/openTicketManager";
    }

    @GetMapping("/ticket/{id}")
    public String goTicket(@PathVariable String id, Model model, HttpSession session) {
        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Optional<Ticket> ticket = ticketService.findById(id);
        if (ticket.isEmpty()) {
            model.addAttribute("msgError", "Ticket not found!");
            return redirectService.pathName(session, "/support");
        }

        String closeDate = "true";
        if (ticket.get().getTicketClosed() == null) { closeDate="false"; }

        ticket.get().setNotes(reverseNoteDateOrder(ticket.get().getNotes()));

        // determine if agent
        boolean isAgent = supportEditor(currentUser);
        if (currentUser.getId().equals(ticket.get().getUser().getId())) {
            // force agent and private false if ticket belongs to current user
            isAgent = false;
        }

        // filter notes if not agent
        List<TicketNote> noteList = new ArrayList<>();
        if (!isAgent) {
            for (TicketNote note : ticket.get().getNotes()) {
                if (!note.isNotePrivate()) {
                    noteList.add(note);
                }
            }
        } else {
            noteList = ticket.get().getNotes();
        }

        model.addAttribute("ticket", ticket.get());
        model.addAttribute("noteList", noteList);
        model.addAttribute("closeDate", closeDate);
        model.addAttribute("isAgent", isAgent);

        redirectService.setHistory(session, "/support/ticket" + id);
        return "support/ticket";
    }

    @GetMapping("/settings")
    public String goSupportSettings(Model model, HttpSession session) {
        // Request Target
        SupportSetting supportSetting = supportSettingService.getSupportSetting();
        BigInteger userId = BigInteger.valueOf(0);
        if (supportSetting.getUser() != null) {
            userId = supportSetting.getUser().getId();
        }
        List<TicketNotifyTarget> targetList = Arrays.asList(TicketNotifyTarget.values());
        List<User> userList = userService.findAllByRoles("ADMIN_WRITE,SUPPORT_AGENT,SUPPORT_SUPERVISOR");

        List<Ticket> tickets = ticketService.findAll();
        Integer completeTickets = 0;
        Integer openTickets = 0;
        for (Ticket ticket : tickets) {
            if (ticket.getTicketStatus().equals(TicketStatus.Closed)) {
                completeTickets++;
            }
            if (!ticket.getTicketStatus().equals(TicketStatus.Closed)) {
                openTickets++;
            }
        }

        model.addAttribute("totalTickets", tickets.size());
        model.addAttribute("openTickets", openTickets);
        model.addAttribute("completeTickets", completeTickets);

        model.addAttribute("priorityList", ticketPriorityService.findAll());
        model.addAttribute("targetList", targetList);
        model.addAttribute("userList", userList);
        model.addAttribute("notifyTarget", supportSetting.getNotifyTarget().toString());
        model.addAttribute("userId", userId);
        model.addAttribute("supportSetting", supportSetting);
        model.addAttribute("slaList", slaService.findAll());
        model.addAttribute("selectedSLA", supportSetting.getDefaultSLAId());


        redirectService.setHistory(session, "/support/settings");
        return "support/settings";
    }

    @GetMapping("/slamanager")
    public String goSLAManager(Model model, HttpSession session) {
        model.addAttribute("slaList", slaService.findAll());

        redirectService.setHistory(session, "/support/slamanager");
        return "support/SLAManager";
    }

    @GetMapping("/new")
    public String goNewTicket(Model model, HttpSession session) {
        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        model.addAttribute("user", currentUser);
        model.addAttribute("priorityList", ticketPriorityService.findAll());
        model.addAttribute("campusList", campusList(currentUser));
        model.addAttribute("departmentList", departmentList(currentUser));

        redirectService.setHistory(session, "/support");
        return "support/ticketNew";
    }

    @PostMapping("/create")
    public String createTicket(@ModelAttribute TicketNewModel ticketNewModel) {
        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        TicketImage ticketImage = null;
        if (ticketNewModel.getImage() != null) {
            ticketImage = ticketImageService.findById(ticketNewModel.getImage()).orElse(null);
        }

        Ticket ticket = ticketService.createTicket(ticketNewModel, currentUser, ticketImage);

        EmailDetails emailDetailsUser =
                new EmailDetails(BigInteger.valueOf(0), currentUser.getContactEmail(),"Your New Support Ticket", "", null );
        BrowserMessage returnMsg = emailService.sendUserTicket(emailDetailsUser, ticket, ticket.getNotes().get(0));

        // determine if agent or agent list
        if (ticket.getAgent() != null) {
            // Notify Agent
            NotificationMessage returnMsg2 = messageService.createMessage(
                    new NotificationMessage(
                            null,
                            "A New Support Ticket Needs Acknowledgement",
                            ticket.getId(),
                            BigInteger.valueOf(0),
                            null,
                            false,
                            false,
                            null,
                            false,
                            BigInteger.valueOf(0),
                            EventModule.Support,
                            EventType.New,
                            ticket.getAgent(),
                            ticket.getNotes().get(0).getId()
                    ));
//            EmailDetails emailDetailsAgent =
//                    new EmailDetails(ticket.getAgent().getContactEmail(),"A New Support Ticket Needs Acknowledgement", "", null );
//            BrowserMessage returnMsg2 = emailService.sendAgentTicket(emailDetailsAgent, ticket, ticket.getNotes().get(0), ticket.getAgent().getId());
        } else {
            for (User agent : ticket.getAgentList()) {
                // Notify Agent
                NotificationMessage returnMsg2 = messageService.createMessage(
                        new NotificationMessage(
                                null,
                                "A New Support Ticket Needs Acceptance",
                                ticket.getId(),
                                BigInteger.valueOf(0),
                                null,
                                false,
                                false,
                                null,
                                false,
                                BigInteger.valueOf(0),
                                EventModule.Support,
                                EventType.New,
                                agent,
                                ticket.getNotes().get(0).getId()
                        ));
//                EmailDetails emailDetailsAgent =
//                        new EmailDetails(agent.getContactEmail(), "A New Support Ticket Needs Acceptance", "", null);
//                BrowserMessage returnMsg2 = emailService.sendAgentListTicket(emailDetailsAgent, ticket, ticket.getNotes().get(0), agent.getId());
            }
        }

        // send any additional notifications
        String superFullName = "[not yet assigned]";
        if (ticket.getAgent() != null) {
            superFullName = ticket.getAgent().getFirstName()+" "+ticket.getAgent().getLastName();
        }
        AppEvent notifyEvent = eventService.createEvent(new AppEvent(
                null,
                "A New Ticket was Submitted by "+currentUser.getFullName(),
                "A New Ticket has been submitted by "+currentUser.getFullName()+ ", and sent to "+superFullName+".",
                ticket.getId(),
                EventModule.Support,
                EventType.New,
                new ArrayList<>()
        ));

        return "redirect:/support/ticket/" + ticket.getId();
    }

    @GetMapping("/user/{id}")
    public String getUserOrders(@RequestParam Optional<String> cycle, @PathVariable BigInteger id, Model model, HttpSession session) {
        Integer currentCycle = Integer.parseInt(session.getAttribute("cycle").toString());
        if (cycle.isPresent()) {
            switch (cycle.get()) {
                case "left":
                    currentCycle--;
                    session.setAttribute("cycle", currentCycle);
                    break;
                case "right":
                    if (currentCycle < 0) {
                        currentCycle++;
                        session.setAttribute("cycle", currentCycle);
                        break;
                    }
                default:
                    currentCycle=0;
                    session.setAttribute("cycle", 0);
            }
        }

        LocalDateTime startDate = getStartDate(session.getAttribute("dateFilter").toString(), currentCycle);
        LocalDateTime endDate = getEndDate(session.getAttribute("dateFilter").toString(), currentCycle);

        Optional<User> user = userService.findById(id);

        if (user.isEmpty()) {
            session.setAttribute("msgError", "User not found.");
            return redirectService.pathName(session, "/support");
        }

        List<Ticket> ticketList = ticketService.findAllByUserAndCycle(user.get().getId(), startDate, endDate);
        List<Ticket> agentTicketList = ticketService.findAllByAgentAndCycle(user.get().getId(), startDate, endDate);
        List<Ticket> ticketMentionList = ticketService.findAllByMentionOpenAndCycle(user.get().getId(), startDate, endDate);

        model.addAttribute("editable", false);
        model.addAttribute("searchedUser", user.get().getFirstName()+" "+user.get().getLastName());
        model.addAttribute("ticketList", ticketList);
        model.addAttribute("agentTicketList", agentTicketList);
        model.addAttribute("ticketMentionList", ticketMentionList);

        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);

        redirectService.setHistory(session, "/support/user/"+id);
        return "support/ticketUser";
    }

    @GetMapping("/campus")
    public String getOpenTicketsByCampus(@RequestParam Optional<String> cycle, Model model, HttpSession session) {
        Integer currentCycle = Integer.parseInt(session.getAttribute("cycle").toString());
        if (cycle.isPresent()) {
            switch (cycle.get()) {
                case "left":
                    currentCycle--;
                    session.setAttribute("cycle", currentCycle);
                    break;
                case "right":
                    if (currentCycle < 0) {
                        currentCycle++;
                        session.setAttribute("cycle", currentCycle);
                        break;
                    }
                default:
                    currentCycle=0;
                    session.setAttribute("cycle", 0);
            }
        }

        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        LocalDateTime startDate = getStartDate(session.getAttribute("dateFilter").toString(), currentCycle);
        LocalDateTime endDate = getEndDate(session.getAttribute("dateFilter").toString(), currentCycle);

        List<Campus> campusList = campusList(currentUser);
        if (campusList.size() == 1) {
            return "redirect:/support/campus/"+campusList.get(0).getName();
        }

        BigInteger departmentID = BigInteger.valueOf(0);
        List<DepartmentRegional> departmentList = departmentList(currentUser);
        if (departmentList.size() == 1) {
            departmentID = departmentList.get(0).getId();
        }

        List<TicketCampus> campusItemList = new ArrayList<>();
        for (Campus campus : campusList) {
            TicketCampus campusItem = new TicketCampus(campus, new ArrayList<>());
            if (departmentID.equals(BigInteger.valueOf(0))) {
                campusItem.setTicketList(ticketService.findAllByCampusAndCycle(campus.getId(), startDate, endDate));
            } else {
                campusItem.setTicketList(ticketService.findAllByDepartmentAndCampusAndCycle(departmentID, campus.getId(), startDate, endDate));
            }
            campusItemList.add(campusItem);
        }


        model.addAttribute("editable", false);
        model.addAttribute("campusItemList", campusItemList);

        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);

        redirectService.setHistory(session, "/support/campus");
        return "support/ticketCampusList";
    }

    @GetMapping("/campus/{name}")
    public String getTicketsByCampus(@RequestParam Optional<String> cycle, @PathVariable String name, Model model, HttpSession session) {
        Integer currentCycle = Integer.parseInt(session.getAttribute("cycle").toString());
        if (cycle.isPresent()) {
            switch (cycle.get()) {
                case "left":
                    currentCycle--;
                    session.setAttribute("cycle", currentCycle);
                    break;
                case "right":
                    if (currentCycle < 0) {
                        currentCycle++;
                        session.setAttribute("cycle", currentCycle);
                        break;
                    }
                default:
                    currentCycle=0;
                    session.setAttribute("cycle", 0);
            }
        }

        name = name.replace("_"," ");

        LocalDateTime startDate = getStartDate(session.getAttribute("dateFilter").toString(), currentCycle);
        LocalDateTime endDate = getEndDate(session.getAttribute("dateFilter").toString(), currentCycle);

        Optional<Campus> campus = campusService.findByName(name);
        List<Ticket> openList = new ArrayList<>();
        List<Ticket> closedList = new ArrayList<>();

        if (campus.isEmpty()) {
            session.setAttribute("msgError", "Campus not found!");
            return redirectService.pathName(session, "/support");
        }

        List<Ticket> ticketList = ticketService.findAllByCampusAndCycle(campus.get().getId(), startDate, endDate);
        for (Ticket ticket : ticketList) {
            if (ticket.getTicketStatus().equals(TicketStatus.Closed)) {
                closedList.add(ticket);
            } else {
                openList.add(ticket);
            }
        }


        model.addAttribute("editable", false);
        model.addAttribute("campus", campus.get());
        model.addAttribute("openList", openList);
        model.addAttribute("closedList", closedList);

        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);

        redirectService.setHistory(session, "/support/campus");
        return "support/ticketCampus";
    }

    @GetMapping("/department")
    public String getOpenTicketsByDepartment(@RequestParam Optional<String> cycle, Model model, HttpSession session) {
        Integer currentCycle = Integer.parseInt(session.getAttribute("cycle").toString());
        if (cycle.isPresent()) {
            switch (cycle.get()) {
                case "left":
                    currentCycle--;
                    session.setAttribute("cycle", currentCycle);
                    break;
                case "right":
                    if (currentCycle < 0) {
                        currentCycle++;
                        session.setAttribute("cycle", currentCycle);
                        break;
                    }
                default:
                    currentCycle=0;
                    session.setAttribute("cycle", 0);
            }
        }

        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        LocalDateTime startDate = getStartDate(session.getAttribute("dateFilter").toString(), currentCycle);
        LocalDateTime endDate = getEndDate(session.getAttribute("dateFilter").toString(), currentCycle);

        List<DepartmentRegional> departmentList = departmentList(currentUser);
        if (departmentList.size() == 1) {
            return "redirect:/support/department/"+departmentList.get(0).getName();
        }

        BigInteger campusId = BigInteger.valueOf(0);
        List<Campus> campusList = campusList(currentUser);
        if (campusList.size() == 1) {
            campusId = campusList.get(0).getId();
        }

        List<TicketDepartment> departmentItemList = new ArrayList<>();
        for (DepartmentRegional department : departmentList) {
            TicketDepartment departmentItem = new TicketDepartment(department, new ArrayList<>());
            if (campusId.equals(BigInteger.valueOf(0))) {
                departmentItem.setTicketList(ticketService.findAllByDepartmentAndCycle(department.getId(), startDate, endDate));
            } else {
                departmentItem.setTicketList(ticketService.findAllByDepartmentAndCampusAndCycle(department.getId(), campusId, startDate, endDate));
            }
            departmentItemList.add(departmentItem);
        }


        model.addAttribute("editable", false);
        model.addAttribute("departmentItemList", departmentItemList);

        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);

        redirectService.setHistory(session, "/support/department");
        return "support/ticketDepartmentList";
    }

    @GetMapping("/department/{name}")
    public String getTicketsByDepartment(@RequestParam Optional<String> cycle, @PathVariable String name, Model model, HttpSession session) {
        Integer currentCycle = Integer.parseInt(session.getAttribute("cycle").toString());
        if (cycle.isPresent()) {
            switch (cycle.get()) {
                case "left":
                    currentCycle--;
                    session.setAttribute("cycle", currentCycle);
                    break;
                case "right":
                    if (currentCycle < 0) {
                        currentCycle++;
                        session.setAttribute("cycle", currentCycle);
                        break;
                    }
                default:
                    currentCycle=0;
                    session.setAttribute("cycle", 0);
            }
        }

        name = name.replace("_"," ");

        LocalDateTime startDate = getStartDate(session.getAttribute("dateFilter").toString(), currentCycle);
        LocalDateTime endDate = getEndDate(session.getAttribute("dateFilter").toString(), currentCycle);

        Optional<DepartmentRegional> department = departmentRegionalService.findByName(name);

        if (department.isEmpty()) {
            session.setAttribute("msgError", "Department not found!");
            return redirectService.pathName(session, "/support");
        }

        List<Ticket> openList = new ArrayList<>();
        List<Ticket> closedList = new ArrayList<>();

        List<Ticket> departmentList = ticketService.findAllByDepartmentAndCycle(department.get().getId(), startDate, endDate);

        for (Ticket ticket : departmentList) {
            if (ticket.getTicketStatus().equals(TicketStatus.Closed)) {
                closedList.add(ticket);
            } else {
                openList.add(ticket);
            }
        }


        model.addAttribute("editable", false);
        model.addAttribute("department", department.get());
        model.addAttribute("openList", openList);
        model.addAttribute("closedList", closedList);

        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);

        redirectService.setHistory(session, "/support/department");
        return "support/ticketDepartment";
    }

    @PostMapping("/search")
    public String searchRequests(@ModelAttribute UniversalSearchModel searchModel) {
        // determine if search type
        switch (searchModel.getSearchType()) {
            case "ticket":
                return "redirect:/support/ticket/"+searchModel.getSearchStringId();
            case "user":
                return "redirect:/support/user/"+searchModel.getSearchId();
            default:
                return "redirect:/support";
        }
    }

    // get edit permission
    private boolean supportEditor(User currentUser) {
        Collection<UserRoles> roles = currentUser.getUserRoles();
        for (UserRoles role : roles) {
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
        for (UserRoles role : roles) {
            if (role.getName().equals("ADMIN_WRITE") || role.getName().equals("SUPPORT_SUPERVISOR")) {
                typeString = "Admin";
            } else if (role.getName().equals("DIRECTOR_WRITE") || role.getName().equals("DIRECTOR_READ")) {
                typeString = "Department";
            } else if (role.getName().equals("CAMPUS_WRITE") || role.getName().equals("CAMPUS_READ")) {
                typeString = "Campus";
            } else if (role.getName().equals("SUPPORT_AGENT")) {
                typeString = "Assigned";
            } else if (role.getName().equals("SUPPORT_WRITE")) {
                typeString = "Submitted";
            }
        }
        return typeString;
    }

    private List<Campus> campusList(User currentUser) {
        List<Campus> campusList = new ArrayList<>();
        Collection<UserRoles> roles = currentUser.getUserRoles();
        for (UserRoles role : roles) {
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
        for (UserRoles role : roles) {
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
        List<User> userListReturn;
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

        if (director != null) {
            userListReturn.add(director);
        }

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

        if (user != null) {
            userListReturn.add(user);
        }

        Collections.sort(userListReturn, new Comparator<User>() {
            @Override
            public int compare(User user1, User user2) {
                return user1.getFullName().compareToIgnoreCase(user2.getFullName());
            }
        });

        return userListReturn;
    }

    private List<TicketNote> reverseNoteDateOrder(List<TicketNote> notes) {
        Collections.sort(notes,new Comparator<TicketNote>() {
            @Override
            public int compare (TicketNote note1, TicketNote note2){
                return note1.getNoteDate().compareTo(note2.getNoteDate());
            }
        });
        Collections.reverse(notes);
        return notes;
    }

    private LocalDateTime getStartDate(String dateSpan, Integer cycle) {
        // parse dateSpan
        String span = dateSpan.substring(0,1);
        String part = dateSpan.substring(1,2);
        //adjust the cycle
        int adjustment = Integer.parseInt(span);
        int cycleAdjustment = adjustment+abs(cycle*adjustment);

        // init date
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        LocalDateTime dateNow = LocalDateTime.now();
        // add leading 0 to month
        Integer month = dateNow.getMonthValue();
        String monthString = month.toString();
        Integer day = dateNow.getDayOfMonth();
        String dayString = day.toString();
        if (monthString.length() < 2) {
            monthString = "0"+monthString;
        }
        if (dayString.length() < 2) {
            dayString = "0"+dayString;
        }
        // create date and convert to LocalDateTime
        String dateString = dateNow.getYear()+"-"+monthString+"-"+ dayString+" 00:00";
        LocalDateTime returnDate = LocalDateTime.parse(dateString, formatter);

        // adjust date based on part
        switch (part) {
            case "Y":
                returnDate = returnDate.minusYears(cycleAdjustment);
                break;
            case "M":
                returnDate = returnDate.minusMonths(cycleAdjustment);
                break;
            case "W":
                returnDate = returnDate.minusWeeks(cycleAdjustment);
                break;
        }
        return returnDate;
    }

    private LocalDateTime getEndDate(String dateSpan, Integer cycle) {
        // parse dateSpan
        String part = dateSpan.substring(1,2);
        //adjust the cycle
        int adjustment = 0;
        int cycleAdjustment = adjustment+abs(cycle);

        // init date
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        LocalDateTime dateNow = LocalDateTime.now();
        // add leading 0 to month
        Integer month = dateNow.getMonthValue();
        String monthString = month.toString();
        Integer day = dateNow.getDayOfMonth();
        String dayString = day.toString();
        if (monthString.length() < 2) {
            monthString = "0"+monthString;
        }
        if (dayString.length() < 2) {
            dayString = "0"+dayString;
        }
        // create date and convert to LocalDateTime
        String dateString = dateNow.getYear()+"-"+monthString+"-"+ dayString+" 24:00";
        LocalDateTime returnDate = LocalDateTime.parse(dateString, formatter);

        // adjust date based on part
        switch (part) {
            case "Y":
                returnDate = returnDate.minusYears(cycleAdjustment);
                break;
            case "M":
                returnDate = returnDate.minusMonths(cycleAdjustment);
                break;
            case "W":
                returnDate = returnDate.minusWeeks(cycleAdjustment);
                break;
        }
//        returnDate = returnDate.plusDays(1);
        return returnDate;
    }

}
