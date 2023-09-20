package net.dahliasolutions.controllers.app;

import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import net.dahliasolutions.models.*;
import net.dahliasolutions.models.campus.Campus;
import net.dahliasolutions.models.department.DepartmentRegional;
import net.dahliasolutions.models.order.OrderNote;
import net.dahliasolutions.models.order.OrderRequest;
import net.dahliasolutions.models.order.OrderStatus;
import net.dahliasolutions.models.records.*;
import net.dahliasolutions.models.support.*;
import net.dahliasolutions.models.user.User;
import net.dahliasolutions.models.user.UserRoles;
import net.dahliasolutions.services.JwtService;
import net.dahliasolutions.services.campus.CampusService;
import net.dahliasolutions.services.department.DepartmentCampusService;
import net.dahliasolutions.services.department.DepartmentRegionalService;
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
@RequestMapping("/api/v1/app/ticket")
public class MobileAppAPITicketController {

    private final JwtService jwtService;
    private final UserService userService;
    private final TicketService ticketService;
    private final UserRolesService rolesService;
    private final TicketImageService ticketImageService;
    private final TicketNoteService noteService;
    private final TicketSLAService slaService;
    private final TicketPriorityService ticketPriorityService;
    private final CampusService campusService;
    private final DepartmentRegionalService departmentRegionalService;
    private final DepartmentCampusService departmentCampusService;
    private final AppServer appServer;

    @GetMapping("/listbyuser")
    public ResponseEntity<List<AppItem>> getTicketsByUser(HttpServletRequest request) {
        APIUser apiUser = getUserFromToken(request);
        if (!apiUser.isValid()) {
            return new ResponseEntity<>(new ArrayList<>(), HttpStatus.FORBIDDEN);
        }

        List<AppItem> appItemList = new ArrayList<>();
        List<Ticket> openAgentTicketList = ticketService.findAllByUser(apiUser.getUser());

        for (Ticket tkt : openAgentTicketList) {
            if (!tkt.getTicketStatus().equals(TicketStatus.Closed)) {
                appItemList.add(new AppItem(
                        tkt.getId(),
                        tkt.getTicketStatus().toString(),
                        tkt.getTicketDetail(),
                        tkt.getTicketDate(),
                        0,
                        tkt.getUser().getFullName(),
                        "tickets"));
            }
        }

        return new ResponseEntity<>(appItemList, HttpStatus.OK);
    }

    @GetMapping("/listbyagent")
    public ResponseEntity<List<AppItem>> getUserTickets(HttpServletRequest request) {
        APIUser apiUser = getUserFromToken(request);
        if (!apiUser.isValid()) {
            return new ResponseEntity<>(new ArrayList<>(), HttpStatus.FORBIDDEN);
        }

        List<AppItem> appItemList = new ArrayList<>();
        List<Ticket> openAgentTicketList = ticketService.findAllByAgentOpenOnly(apiUser.getUser());

        for (Ticket tkt : openAgentTicketList) {
            appItemList.add(new AppItem(
                    tkt.getId(),
                    tkt.getTicketStatus().toString(),
                    tkt.getTicketDetail(),
                    tkt.getTicketDate(),
                    0,
                    tkt.getUser().getFullName(),
                    "tickets"));
        }

        return new ResponseEntity<>(appItemList, HttpStatus.OK);
    }

    @GetMapping("/listbyincluded")
    public ResponseEntity<List<AppItem>> getUserTicketMentions(HttpServletRequest request) {
        APIUser apiUser = getUserFromToken(request);
        if (!apiUser.isValid()) {
            return new ResponseEntity<>(new ArrayList<>(), HttpStatus.FORBIDDEN);
        }

        List<AppItem> appItemList = new ArrayList<>();
        List<Ticket> openAgentTicketList = ticketService.findAllByMentionOpenOnly(apiUser.getUser());

        for (Ticket tkt : openAgentTicketList) {
            appItemList.add(new AppItem(
                    tkt.getId(),
                    tkt.getTicketStatus().toString(),
                    tkt.getTicketDetail(),
                    tkt.getTicketDate(),
                    0,
                    tkt.getUser().getFullName(),
                    "tickets"));
        }

        return new ResponseEntity<>(appItemList, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AppTicket> getTicketById(@PathVariable String id, HttpServletRequest request) {
        APIUser apiUser = getUserFromToken(request);
        if (!apiUser.isValid()) {
            return new ResponseEntity<>(new AppTicket(), HttpStatus.FORBIDDEN);
        }

        Optional<Ticket> ticket = ticketService.findById(id);
        if (ticket.isEmpty()) {
            return new ResponseEntity<>(new AppTicket(), HttpStatus.BAD_REQUEST);
        }

        boolean closeDate = true;
        if (ticket.get().getTicketClosed() == null) { closeDate=false; }

        ticket.get().setNotes(reverseNoteDateOrder(ticket.get().getNotes()));

        // determine if agent
        boolean isAgent = supportEditor(apiUser.getUser());
        if (apiUser.getUser().getId().equals(ticket.get().getUser().getId())) {
            // force agent and private false if ticket belongs to current user
            isAgent = false;
        }

        // create the App Ticket
        AppTicket t = new AppTicket(
                ticket.get().getId(),
                ticket.get().getTicketDate(),
                ticket.get().getTicketDue(),
                ticket.get().getTicketClosed(),
                ticket.get().getTicketDetail(),
                ticket.get().getPriority(),
                isAgent,
                closeDate,
                ticket.get().getSla(),
                ticket.get().getTicketStatus(),
                ticket.get().getCampus(),
                ticket.get().getDepartment(),
                ticket.get().getUser(),
                ticket.get().getAgent()

        );

        return new ResponseEntity<>(t, HttpStatus.OK);
    }

    @GetMapping("/notelist/{id}")
    public ResponseEntity<List<AppTicketNote>> getTicketNotesById(@PathVariable String id, HttpServletRequest request) {
        APIUser apiUser = getUserFromToken(request);
        if (!apiUser.isValid()) {
            return new ResponseEntity<>(new ArrayList<>(), HttpStatus.FORBIDDEN);
        }

        Optional<Ticket> ticket = ticketService.findById(id);
        if (ticket.isEmpty()) {
            return new ResponseEntity<>(new ArrayList<>(), HttpStatus.BAD_REQUEST);
        }

        ticket.get().setNotes(reverseNoteDateOrder(ticket.get().getNotes()));

        // determine if agent
        boolean isAgent = supportEditor(apiUser.getUser());
        if (apiUser.getUser().getId().equals(ticket.get().getUser().getId())) {
            // force agent and private false if ticket belongs to current user
            isAgent = false;
        }

        // filter notes if not agent
        List<AppTicketNote> noteList = new ArrayList<>();
        if (!isAgent) {
            for (TicketNote note : ticket.get().getNotes()) {
                if (!note.isNotePrivate()) {
                    noteList.add(new AppTicketNote(
                            note.getId(),
                            note.getNoteDate(),
                            note.isNotePrivate(),
                            note.isAgentNote(),
                            note.getUser().getFullName(),
                            note.getDetail(),
                            note.getImages()
                    ));
                }
            }
        } else {
            for (TicketNote note : ticket.get().getNotes()) {
                noteList.add(new AppTicketNote(
                        note.getId(),
                        note.getNoteDate(),
                        note.isNotePrivate(),
                        note.isAgentNote(),
                        note.getUser().getFullName(),
                        note.getDetail(),
                        note.getImages()
                ));
            }
        }

        return new ResponseEntity<>(noteList, HttpStatus.OK);
    }

    @GetMapping("/agentlist/{id}")
    public ResponseEntity<List<User>> getTicketAgentsById(@PathVariable String id, HttpServletRequest request) {
        APIUser apiUser = getUserFromToken(request);
        if (!apiUser.isValid()) {
            return new ResponseEntity<>(new ArrayList<>(), HttpStatus.FORBIDDEN);
        }

        Optional<Ticket> ticket = ticketService.findById(id);
        if (ticket.isEmpty()) {
            return new ResponseEntity<>(new ArrayList<>(), HttpStatus.BAD_REQUEST);
        }

        return new ResponseEntity<>(ticket.get().getAgentList(), HttpStatus.OK);
    }

    @PostMapping("/getaccept")
    public ResponseEntity<SingleStringModel> getTicketAccept(@ModelAttribute SingleStringModel requestModel, HttpServletRequest request) {
        APIUser apiUser = getUserFromToken(request);
        if (!apiUser.isValid()) {
            return new ResponseEntity<>(new SingleStringModel("Access denied!"), HttpStatus.FORBIDDEN);
        }

        Optional<Ticket> ticket = ticketService.findById(requestModel.name());
        if (ticket.isEmpty()) {
            return new ResponseEntity<>(new SingleStringModel("Not Found"), HttpStatus.NOT_FOUND);
        }

        if (ticket.get().getAgent() == null) {
            return new ResponseEntity<>(new SingleStringModel("Acknowledge"), HttpStatus.OK);
        }

        return new ResponseEntity<>(new SingleStringModel("Not Supervisor"), HttpStatus.BAD_REQUEST);
    }

    @PostMapping("/setaccept")
    public ResponseEntity<SingleStringModel> setTicketAccept(@ModelAttribute SingleStringModel requestModel, HttpServletRequest request) {
        APIUser apiUser = getUserFromToken(request);
        if (!apiUser.isValid()) {
            return new ResponseEntity<>(new SingleStringModel("Access denied!"), HttpStatus.FORBIDDEN);
        }

        Optional<Ticket> ticket = ticketService.findById(requestModel.name());
        if (ticket.isEmpty()) {
            return new ResponseEntity<>(new SingleStringModel("Not Found"), HttpStatus.NOT_FOUND);
        }
        ticket.get().setAgentList(
                removeFromSupervisorList(apiUser.getUser(), ticket.get().getAgentList()));

        ticket.get().setAgent(apiUser.getUser());
        ticketService.save(ticket.get());
        TicketNote ticketNote = noteService.createTicketNote(
                new TicketNote(null, null, false,
                        true, "Ticket was accepted by "+apiUser.getUser().getFullName()+".",
                        new ArrayList<>(), apiUser.getUser(), ticket.get()));

        return new ResponseEntity<>(new SingleStringModel("success"), HttpStatus.OK);
    }

    @GetMapping("/agentoptions")
    public ResponseEntity<List<User>> getAgentOptions(HttpServletRequest request) {
        APIUser apiUser = getUserFromToken(request);
        if (!apiUser.isValid()) {
            return new ResponseEntity<>(new ArrayList<>(), HttpStatus.FORBIDDEN);
        }

        Collection<UserRoles> roles = getSupervisorCollection();
        List<User> userList = new ArrayList<>();
        List<User> allUsers = userService.findAll();

        for (User user : allUsers) {
            for (UserRoles role : user.getUserRoles()) {
                if (roles.contains(role)) {
                    userList.add(user);
                    break;
                }
            }
        }

        return new ResponseEntity<>(userList, HttpStatus.OK);
    }

    @GetMapping("/ticketstatusoptions")
    public ResponseEntity<List<TicketStatus>> getTicketStatusOptions(HttpServletRequest request) {
        APIUser apiUser = getUserFromToken(request);
        if (!apiUser.isValid()) {
            return new ResponseEntity<>(new ArrayList<>(), HttpStatus.FORBIDDEN);
        }

        return new ResponseEntity<>(Arrays.asList(TicketStatus.values()), HttpStatus.OK);
    }

    @GetMapping("/slaoptions")
    public ResponseEntity<List<SLA>> getSLAOptions(HttpServletRequest request) {
        APIUser apiUser = getUserFromToken(request);
        if (!apiUser.isValid()) {
            return new ResponseEntity<>(new ArrayList<>(), HttpStatus.FORBIDDEN);
        }

        return new ResponseEntity<>(slaService.findAll(), HttpStatus.OK);
    }

    @PostMapping("/uploadticketimage")
    public TicketImage uploadTicketImage(@RequestPart("imageFile") MultipartFile imageFile) {
        // get the image save location and verify directories
        String fileName = StringUtils.cleanPath(imageFile.getOriginalFilename());
        String uploadDir = appServer.getResourceDir() + "/support/images";
        String fileURL = appServer.getResourceURL() + "/support/images/" + fileName;
        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            try {
                Files.createDirectories(uploadPath);
            } catch (IOException e) {
                System.out.println("error creating dir");
            }
        }

        // try to save file
        try {
            InputStream inputStream = imageFile.getInputStream();
            Path filePath = uploadPath.resolve(fileName);
            Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            System.out.println("error saving file");
        }

        TicketImage ticketImage = new TicketImage(null, fileName, fileName, fileURL);

        return ticketImageService.createStoredImage(ticketImage);
    }

    @PostMapping("/removeticketimage")
    public SingleBigIntegerModel removeStoredTicketImage(@ModelAttribute SingleBigIntegerModel image){
        Optional<TicketImage> ticketImage = ticketImageService.findById(image.id());
        if (ticketImage.isPresent()) {
            ticketImageService.deleteById(ticketImage.get().getId());
        }
        return image;
    }

    @PostMapping("/postnote")
    public ResponseEntity<TicketNote> newTicketNote(@ModelAttribute TicketNoteModel noteModel, HttpServletRequest request) {
        APIUser apiUser = getUserFromToken(request);
        if (!apiUser.isValid()) {
            return new ResponseEntity<>(new TicketNote(), HttpStatus.FORBIDDEN);
        }

        Optional<Ticket> ticket = ticketService.findById(noteModel.ticketId());

        if (ticket.isEmpty()) {
            return new ResponseEntity<>(new TicketNote(), HttpStatus.BAD_REQUEST);
        }

        boolean isPrivate = noteModel.isPrivate();
        boolean isAgent = supportEditor(apiUser.getUser());

        if (apiUser.getUser().getId().equals(ticket.get().getUser().getId())) {
            // force agent and private false if ticket belongs to current user
            isAgent = false;
            isPrivate = false;
        }

        List<String> items = Arrays.asList(noteModel.images().split("\s"));
        ArrayList<TicketImage> images = new ArrayList<>();
        for (String s : items) {
            if (!s.equals("")) {
                try {
                    int i = Integer.parseInt(s);
                    Optional<TicketImage> img = ticketImageService.findById(BigInteger.valueOf(i));
                    if (img.isPresent()) {
                        images.add(img.get());
                    }
                } catch (Error e) {
                    System.out.println(e);
                }
            }
        }

        TicketNote note = noteService.createTicketNote(new TicketNote(null, null, isPrivate,
                isAgent, noteModel.detail(), images, apiUser.getUser(), ticket.get()));
        ticket.get().getNotes().add(note);
        ticketService.save(ticket.get());

        return new ResponseEntity<>(note, HttpStatus.OK);
    }

    @PostMapping("/poststatus")
    public ResponseEntity<SingleStringModel> updateRequestStatus(@ModelAttribute TicketStatusModel statusModel, HttpServletRequest request) {
        APIUser apiUser = getUserFromToken(request);
        if (!apiUser.isValid()) {
            return new ResponseEntity<>(new SingleStringModel(""), HttpStatus.FORBIDDEN);
        }

        TicketStatus setStatus = TicketStatus.valueOf(statusModel.status());
        Optional<Ticket> ticket = ticketService.findById(statusModel.id());

        if (ticket.isEmpty()) {
            return new ResponseEntity<>(new SingleStringModel(""), HttpStatus.BAD_REQUEST);
        }

        String noteDetail = statusModel.note();
        if (noteDetail.equals("")) {
            noteDetail = "The status was updated to "+statusModel.status()+" by "+apiUser.getUser().getFullName();
        }

        if (ticket.isPresent()) {
            ticket.get().setTicketStatus(TicketStatus.valueOf(statusModel.status()));
            if (setStatus.equals(TicketStatus.Closed)) {
                ticket.get().setTicketClosed(LocalDateTime.now());
            }
            ticketService.save(ticket.get());

            TicketNote ticketNote = noteService.createTicketNote(
                    new TicketNote(null, null, false,
                            true, noteDetail, new ArrayList<>(), apiUser.getUser(), ticket.get()));
        }

        return new ResponseEntity<>(new SingleStringModel(setStatus.toString()), HttpStatus.OK);
    }

    @PostMapping("/postsla")
    public ResponseEntity<SingleStringModel> updateRequestStatus(@ModelAttribute BigIntegerStringModel slaModel, HttpServletRequest request) {
        APIUser apiUser = getUserFromToken(request);
        if (!apiUser.isValid()) {
            return new ResponseEntity<>(new SingleStringModel(""), HttpStatus.FORBIDDEN);
        }

        Optional<SLA> sla = slaService.findById(slaModel.id());
        Optional<Ticket> ticket = ticketService.findById(slaModel.name());

        if (sla.isPresent()) {
            String noteDetail = "The SLA was updated to " + sla.get().getName() + " by " +
                    apiUser.getUser().getFullName();
            if (ticket.isPresent()) {
                LocalDateTime newDueDate = ticket.get().getTicketDate().plusHours(sla.get().getCompletionDue());

                ticket.get().setSla(sla.get());
                ticket.get().setTicketDue(newDueDate);
                ticketService.save(ticket.get());

                TicketNote ticketNote = noteService.createTicketNote(
                        new TicketNote(null, null, true,
                                true, noteDetail, new ArrayList<>(), apiUser.getUser(), ticket.get()));

                return new ResponseEntity<>(new SingleStringModel(sla.get().getName()), HttpStatus.OK);
            }
        }
        return new ResponseEntity<>(new SingleStringModel(""), HttpStatus.BAD_REQUEST);
    }

    @PostMapping("/addagent")
    public ResponseEntity<SingleStringModel> addAgentToRequest(@ModelAttribute AddTicketAgentModel supervisorModel, HttpServletRequest request) {
        APIUser apiUser = getUserFromToken(request);
        if (!apiUser.isValid()) {
            return new ResponseEntity<>(new SingleStringModel(""), HttpStatus.FORBIDDEN);
        }

        Optional<Ticket> ticket = ticketService.findById(supervisorModel.id());
        Optional<User> newSuper = userService.findById(supervisorModel.userId());
        String noteDetail = "";
        if (ticket.isPresent()) {
            if (newSuper.isPresent()) {
                if (supervisorModel.primary()) {
                    noteDetail = newSuper.get().getFirstName()+" "+newSuper.get().getLastName()+" was set as primary agent on ticket.";

                    ticket.get().setAgentList(
                            addToSupervisorList(ticket.get().getAgent(), ticket.get().getAgentList()));

                    ticket.get().setAgent(newSuper.get());
                    ticketService.save(ticket.get());
                    TicketNote ticketNote = noteService.createTicketNote(
                            new TicketNote(null, null, false,
                                    true, noteDetail, new ArrayList<>(), apiUser.getUser(), ticket.get()));
                } else {
                    noteDetail = newSuper.get().getFullName()+" was add to the ticket.";

                    ticket.get().setAgentList(
                            addToSupervisorList(newSuper.get(), ticket.get().getAgentList()));

                    ticketService.save(ticket.get());
                    TicketNote ticketNote = noteService.createTicketNote(
                            new TicketNote(null, null, true,
                                    true, noteDetail, new ArrayList<>(), apiUser.getUser(), ticket.get()));
                }
                return new ResponseEntity<>(new SingleStringModel(newSuper.get().getFullName()), HttpStatus.OK);
            }
        }
        return new ResponseEntity<>(new SingleStringModel(""), HttpStatus.BAD_REQUEST);
    }

    @PostMapping("/removeagent")
    public ResponseEntity<SingleStringModel> removeSupervisorToRequest(@ModelAttribute AddTicketAgentModel supervisorModel, HttpServletRequest request) {
        APIUser apiUser = getUserFromToken(request);
        if (!apiUser.isValid()) {
            return new ResponseEntity<>(new SingleStringModel(""), HttpStatus.FORBIDDEN);
        }

        Optional<Ticket> ticket = ticketService.findById(supervisorModel.id());
        Optional<User> newSuper = userService.findById(supervisorModel.userId());
        boolean required = false;

        String noteDetail = "";
        if (ticket.isPresent()) {
            if (newSuper.isPresent()) {
                noteDetail = newSuper.get().getFirstName()+" "+newSuper.get().getLastName()+" was removed from ticket.";

                ticket.get().setAgentList(
                        removeFromSupervisorList(newSuper.get(), ticket.get().getAgentList()));

                ticketService.save(ticket.get());
                TicketNote ticketNote = noteService.createTicketNote(
                        new TicketNote(null, null, true,
                                true, noteDetail, new ArrayList<>(), apiUser.getUser(), ticket.get()));
                return new ResponseEntity<>(new SingleStringModel(""), HttpStatus.OK);
            }
        }
        return new ResponseEntity<>(new SingleStringModel(""), HttpStatus.BAD_REQUEST);
    }


    @GetMapping("/getprioritylist")
    public ResponseEntity<List<TicketPriority>> getTicketPriorityList(HttpServletRequest request) {
        APIUser apiUser = getUserFromToken(request);
        if (!apiUser.isValid()) {
            return new ResponseEntity<>(new ArrayList<>(), HttpStatus.FORBIDDEN);
        }

        return new ResponseEntity<>(ticketPriorityService.findAll(), HttpStatus.OK);
    }

    @GetMapping("/getcampuslist")
    public ResponseEntity<List<Campus>> getTicketCampusList(HttpServletRequest request) {
        APIUser apiUser = getUserFromToken(request);
        if (!apiUser.isValid()) {
            return new ResponseEntity<>(new ArrayList<>(), HttpStatus.FORBIDDEN);
        }

        return new ResponseEntity<>(campusList(apiUser.getUser()), HttpStatus.OK);
    }

    @GetMapping("/getdepartmentlist")
    public ResponseEntity<List<DepartmentRegional>> getTicketDepartmentList(HttpServletRequest request) {
        APIUser apiUser = getUserFromToken(request);
        if (!apiUser.isValid()) {
            return new ResponseEntity<>(new ArrayList<>(), HttpStatus.FORBIDDEN);
        }

        return new ResponseEntity<>(departmentList(apiUser.getUser()), HttpStatus.OK);
    }

    @PostMapping("newticket")
    public ResponseEntity<SingleStringModel> setNewTicket(TicketNewModel ticketNewModel, HttpServletRequest request) {
        APIUser apiUser = getUserFromToken(request);
        if (!apiUser.isValid()) {
            return new ResponseEntity<>(new SingleStringModel(""), HttpStatus.FORBIDDEN);
        }

        Ticket ticket = ticketService.createTicket(ticketNewModel, apiUser.getUser(), null);

        return new ResponseEntity<>(new SingleStringModel(ticket.getId()), HttpStatus.OK);
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

    private Collection<UserRoles> getSupervisorCollection() {
        Collection<UserRoles> roles = new ArrayList<>();
        roles.add(rolesService.findByName("ADMIN_WRITE").get());
        roles.add(rolesService.findByName("SUPPORT_WRITE").get());
        roles.add(rolesService.findByName("SUPPORT_SUPERVISOR").get());
        return roles;
    }

    private List<User> removeFromSupervisorList(User user, List<User> list){
        for (User su : list) {
            if (su.getId().equals(user.getId())) {
                list.remove(su);
                break;
            }
        }
        return list;
    }

    private List<User> addToSupervisorList(User user, List<User> list){
        for (User su : list) {
            if (su.getId().equals(user.getId())) {
                return list;
            }
        }
        list.add(user);
        return list;
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

}
