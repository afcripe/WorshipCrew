package net.dahliasolutions.controllers.support;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import net.dahliasolutions.models.*;
import net.dahliasolutions.models.mail.EmailDetails;
import net.dahliasolutions.models.order.OrderItem;
import net.dahliasolutions.models.order.OrderNote;
import net.dahliasolutions.models.order.OrderRequest;
import net.dahliasolutions.models.order.OrderStatus;
import net.dahliasolutions.models.records.*;
import net.dahliasolutions.models.support.*;
import net.dahliasolutions.models.user.User;
import net.dahliasolutions.models.user.UserRoles;
import net.dahliasolutions.services.EventService;
import net.dahliasolutions.services.mail.EmailService;
import net.dahliasolutions.services.mail.NotificationMessageService;
import net.dahliasolutions.services.support.*;
import net.dahliasolutions.services.user.UserRolesService;
import net.dahliasolutions.services.user.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.rmi.registry.LocateRegistry;
import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/v1/support")
@RequiredArgsConstructor
public class SupportAPIController {

    private final TicketPriorityService ticketPriorityService;
    private final UserService userService;
    private final SupportSettingService supportSettingService;
    private final TicketSLAService slaService;
    private final TicketService ticketService;
    private final TicketImageService ticketImageService;
    private final TicketNoteService noteService;
    private final UserRolesService rolesService;
    private final EmailService emailService;
    private final NotificationMessageService messageService;
    private final EventService eventService;

    @GetMapping("")
    public List<Ticket> getSupportTickets() {
        return null;
    }

    @GetMapping("/priority/getnextdisplay")
    public SingleIntModel getNextDisplayOrder() {
        return new SingleIntModel(ticketPriorityService.getNextDisplayOrder());
    }

    @PostMapping("/priority/getpriority")
    public TicketPriority getNextDisplayOrder(@ModelAttribute SingleBigIntegerModel bigIntegerModel) {
        Optional<TicketPriority> priority = ticketPriorityService.findById(bigIntegerModel.id());
        if (priority.isPresent()) {
            return priority.get();
        }
        return new TicketPriority(BigInteger.valueOf(0), 0, "");
    }

    @PostMapping("/priority/new")
    public TicketPriority postNewCategory(@ModelAttribute TicketPriority ticketPriorityModel) {
        Optional<TicketPriority> priority = ticketPriorityService.findByPriorityLikeIgnoreCase(ticketPriorityModel.getPriority());
        if (priority.isEmpty()) {
            return ticketPriorityService.createPriority(ticketPriorityModel);
        }
        return priority.get();
    }

    @PostMapping("/priority/update")
    public TicketPriority updateCategory(@ModelAttribute TicketPriority ticketPriorityModel) {
        Optional<TicketPriority> priority = ticketPriorityService.findById(ticketPriorityModel.getId());
        Optional<TicketPriority> existingPriority = ticketPriorityService.findByPriorityLikeIgnoreCase(ticketPriorityModel.getPriority());

        if (existingPriority.isPresent() && priority.isPresent()) {
            if (!existingPriority.get().getId().equals(priority.get().getId())) {
                return ticketPriorityModel;
            }
        }
        if (priority.isPresent()) {
            priority.get().setPriority(ticketPriorityModel.getPriority());
            priority.get().setDisplayOrder(ticketPriorityModel.getDisplayOrder());
            return ticketPriorityService.save(priority.get());
        }
        return ticketPriorityModel;
    }

    @PostMapping("/priority/delete")
    public TicketPriority deleteNewCategory(@ModelAttribute TicketPriority ticketPriorityModel) {
        Optional<TicketPriority> priority = ticketPriorityService.findById(ticketPriorityModel.getId());
        if (priority.isPresent()) {
            ticketPriorityService.deleteById(priority.get().getId());
        }
        return priority.get();
    }

    @PostMapping("/supportsetting/update")
    public String updateSupportNotify(@ModelAttribute SingleStringModel notifyModel, @ModelAttribute SingleBigIntegerModel userModel) {
        TicketNotifyTarget target = TicketNotifyTarget.valueOf(notifyModel.name());
        Optional<User> user = userService.findById(userModel.id());

        supportSettingService.setSupportNotifyTarget(target);
        if (target.equals(TicketNotifyTarget.User) && user.isPresent()) {
            supportSettingService.setUser(user.get());
        }

        return "true";
    }


    @PostMapping("/supportsetting/updatesla")
    public String updateSupportNotify(@ModelAttribute SingleBigIntegerModel intModel) {
        supportSettingService.setDefaultSLAId(intModel.id());
        return "true";
    }

    @PostMapping("/prefix/update")
    public SingleStringModel updateSupportNotify(@ModelAttribute SingleStringModel settingPrefix) {
        supportSettingService.setIdPrefix(settingPrefix.name());
        return settingPrefix;
    }

    @PostMapping("/sla/get")
    public SLA getSLAById(@ModelAttribute SingleBigIntegerModel intModel) {
        Optional<SLA> sla = slaService.findById(intModel.id());
        return sla.orElseGet(SLA::new);
    }

    @PostMapping("/sla/update")
    public SLA updateSLA(@ModelAttribute SLA slaModel) {
        Optional<SLA> sla = slaService.findById(slaModel.getId());

        if (sla.isPresent()) {
            sla.get().setName(slaModel.getName());
            sla.get().setDescription(slaModel.getDescription());
            sla.get().setCompletionDue(slaModel.getCompletionDue());

            slaService.save(sla.get());
            return sla.get();
        }

        return slaModel;
    }

    @PostMapping("/sla/delete")
    public SingleBigIntegerModel deleteSLA(@ModelAttribute SingleBigIntegerModel intModel) {
        Optional<SLA> sla = slaService.findById(intModel.id());
        sla.ifPresent(serviceLevel -> slaService.deleteById(serviceLevel.getId()));
        return intModel;
    }

    @PostMapping("/sla/new")
    public BigIntegerStringModel updateOrderNotification(@ModelAttribute BigIntegerStringModel slaModel) {
        SLA sla = new SLA(null, slaModel.name(), "", 0);
        sla = slaService.save(sla);
        return new BigIntegerStringModel(sla.getId(), sla.getName());
    }

    @PostMapping("/note/new")
    public TicketNote newTicketNote(@ModelAttribute TicketNoteModel noteModel) {
        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        Optional<Ticket> ticket = ticketService.findById(noteModel.ticketId());

        if (ticket.isEmpty()) {
            return new TicketNote();
        }

        boolean isPrivate = noteModel.isPrivate();

        boolean isAgent = supportEditor(currentUser);
        if (currentUser.getId().equals(ticket.get().getUser().getId())) {
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
                isAgent, noteModel.detail(), images, currentUser, ticket.get()));
        ticket.get().getNotes().add(note);
        ticketService.save(ticket.get());

        if (isAgent) {
            if (!isPrivate) {
                // email note user on if public
                EmailDetails emailDetailsUser =
                        new EmailDetails(ticket.get().getUser().getContactEmail(), "Support Ticket " + ticket.get().getId() + " has been updated", "", null);
                emailService.sendUserUpdateTicket(emailDetailsUser, ticket.get(), note);
            }
        } else {
            // only send if assigned to agent
            if (ticket.get().getAgent() != null) {
                NotificationMessage returnMsg2 = messageService.createMessage(
                        new NotificationMessage(
                                null,
                                "Support Ticket "+ticket.get().getId()+" has been updated",
                                ticket.get().getId(),
                                BigInteger.valueOf(0),
                                null,
                                false,
                                false,
                                null,
                                false,
                                BigInteger.valueOf(0),
                                EventModule.Support,
                                EventType.Updated,
                                ticket.get().getAgent(),
                                note.getId()
                        ));
//                EmailDetails emailDetailsAgent =
//                        new EmailDetails(ticket.get().getAgent().getContactEmail(),"Support Ticket "+ticket.get().getId()+" has been updated", "", null );
//                BrowserMessage returnMsg2 = emailService.sendAgentUpdateTicket(emailDetailsAgent, ticket.get(), note);
            }
        }

        // send any additional notifications
        AppEvent notifyEvent = eventService.createEvent(new AppEvent(
                null,
                "Ticket "+ticket.get().getId()+" was updated by "+currentUser.getFullName(),
                "A note was added to Ticket "+ticket.get().getId()+" by "+currentUser.getFullName(),
                ticket.get().getId(),
                EventModule.Support,
                EventType.Updated,
                new ArrayList<>()
        ));

        return note;
    }

    @PostMapping("/getnote")
    public TicketNote getTicketNote(@ModelAttribute SingleBigIntegerModel idModel) {
        Optional<TicketNote> note = noteService.findById(idModel.id());

        if (note.isEmpty()){
            return new TicketNote();
        }

        return note.get();
    }

    @PostMapping("/editnote")
    public TicketNote editTicketNote(@ModelAttribute TicketNoteEditModel noteModel) {
        Optional<TicketNote> note = noteService.findById(noteModel.id());

        if (note.isEmpty()){
            return new TicketNote();
        }

        note.get().setDetail(noteModel.detail());
        note.get().setNotePrivate(noteModel.isPrivate());
        noteService.save(note.get());

        return note.get();
    }

    @PostMapping("/deletenote")
    public TicketNote deleteTicketNote(@ModelAttribute SingleBigIntegerModel intModel) {
        Optional<TicketNote> note = noteService.findById(intModel.id());

        if (note.isPresent()){
            List<TicketImage> images = note.get().getImages();
            noteService.deleteById(intModel.id());
            for (TicketImage image : images) {
//                try {
//                    Files.deleteIfExists(Paths.get(image.getFileLocation()));
//                } catch (IOException e) {
//                    System.out.println("File not Deleted");
//                }
                ticketImageService.deleteById(image.getId());
            }

        }

            return new TicketNote();
    }

    @GetMapping("/getstatusoptions")
    public List<TicketStatus> getTicketStatusOptions(){
        return Arrays.asList(TicketStatus.values());
    }

    @GetMapping("/getsupervisors")
    public List<User> getSupervisors(){
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
        return userList;
    }

    @PostMapping("/changestatus")
    public TicketStatusModel updateRequestStatus(@ModelAttribute TicketStatusModel statusModel) {
        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        TicketStatus setStatus = TicketStatus.valueOf(statusModel.status());
        Optional<Ticket> ticket = ticketService.findById(statusModel.id());

        String noteDetail = statusModel.note();
        if (noteDetail.equals("")) {
            noteDetail = "The status was updated to "+statusModel.status()+" by "+currentUser.getFirstName()+" "+currentUser.getLastName();
        }

        if (ticket.isPresent()) {
            ticket.get().setTicketStatus(TicketStatus.valueOf(statusModel.status()));
            if (setStatus.equals(TicketStatus.Closed)) {
                ticket.get().setTicketClosed(LocalDateTime.now());
            } else {
                ticket.get().setTicketClosed(null);
            }
            ticketService.save(ticket.get());

            TicketNote ticketNote = noteService.createTicketNote(
                    new TicketNote(null, null, false,
            true, noteDetail, new ArrayList<>(), currentUser, ticket.get()));

            // email ticket user
            EmailDetails emailDetailsUser =
                    new EmailDetails(ticket.get().getUser().getContactEmail(),
                            "Support Ticket "+ticket.get().getId()+" status has been changed to "+statusModel.status(),
                            "", null );
            BrowserMessage returnMsg = emailService.sendUserUpdateTicket(emailDetailsUser, ticket.get(), ticketNote);

            // send any additional notifications
            AppEvent notifyEvent = eventService.createEvent(new AppEvent(
                    null,
                    "Ticket "+ticket.get().getId()+" status was updated by "+currentUser.getFullName(),
                    "The status of Ticket "+ticket.get().getId()+" was changed to "+statusModel.status()+" by "+currentUser.getFullName(),
                    ticket.get().getId(),
                    EventModule.Support,
                    EventType.Updated,
                    new ArrayList<>()
            ));
            //closed
            if (setStatus.equals(TicketStatus.Closed)) {
                notifyEvent.setType(EventType.Closed);
                eventService.createEvent(notifyEvent);
            }
        }

        return statusModel;
    }

    @PostMapping("/addsupervisor")
    public AddTicketAgentModel addSupervisorToRequest(@ModelAttribute AddTicketAgentModel supervisorModel) {
        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

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
                        true, noteDetail, new ArrayList<>(), currentUser, ticket.get()));
                } else {
                    noteDetail = newSuper.get().getFirstName()+" "+newSuper.get().getLastName()+" was add to the ticket.";

                    ticket.get().setAgentList(
                            addToSupervisorList(newSuper.get(), ticket.get().getAgentList()));

                    ticketService.save(ticket.get());
                    TicketNote ticketNote = noteService.createTicketNote(
                            new TicketNote(null, null, true,
                                    true, noteDetail, new ArrayList<>(), currentUser, ticket.get()));
                }
            }
            // send any additional notifications
            AppEvent notifyEvent = eventService.createEvent(new AppEvent(
                    null,
                    newSuper.get().getFullName()+" was added to Ticket "+ticket.get().getId(),
                    newSuper.get().getFullName()+" was added as an agent to Ticket "+ticket.get().getId()+" by "+currentUser.getFullName(),
                    ticket.get().getId(),
                    EventModule.Support,
                    EventType.Updated,
                    new ArrayList<>()
            ));
        }
        return supervisorModel;
    }

    @PostMapping("/removesupervisor")
    public AddTicketAgentModel removeSupervisorToRequest(@ModelAttribute AddTicketAgentModel supervisorModel) {
        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

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
                true, noteDetail, new ArrayList<>(), currentUser, ticket.get()));
            }
            // send any additional notifications
            AppEvent notifyEvent = eventService.createEvent(new AppEvent(
                    null,
                    newSuper.get().getFullName()+" was removed from Ticket "+ticket.get().getId(),
                    newSuper.get().getFullName()+" was removed from Ticket "+ticket.get().getId()+" by "+currentUser.getFullName(),
                    ticket.get().getId(),
                    EventModule.Support,
                    EventType.Updated,
                    new ArrayList<>()
            ));
        }
        return supervisorModel;
    }

    @GetMapping("/getslaoptions")
    public List<SLA> getSLAOptions() {
        return slaService.findAll();
    }

    @PostMapping("/changesla")
    public SLA updateTicketSLA(@ModelAttribute BigIntegerStringModel slaModel) {
        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        Optional<SLA> sla = slaService.findById(slaModel.id());
        Optional<Ticket> ticket = ticketService.findById(slaModel.name());

        if (sla.isPresent()) {
            String noteDetail = "The SLA was updated to " + sla.get().getName() + " by " +
                    currentUser.getFirstName() + " " + currentUser.getLastName();
            if (ticket.isPresent()) {
                LocalDateTime newDueDate = ticket.get().getTicketDate().plusHours(sla.get().getCompletionDue());

                ticket.get().setSla(sla.get());
                ticket.get().setTicketDue(newDueDate);
                ticketService.save(ticket.get());

                TicketNote ticketNote = noteService.createTicketNote(
                        new TicketNote(null, null, true,
                                true, noteDetail, new ArrayList<>(), currentUser, ticket.get()));

                // send any additional notifications
                AppEvent notifyEvent = eventService.createEvent(new AppEvent(
                        null,
                        "SLA on Ticket "+ticket.get().getId()+" was changed to "+sla.get().getName(),
                        "SLA on Ticket "+ticket.get().getId()+" was changed to "+sla.get().getName()+" by "+currentUser.getFullName(),
                        ticket.get().getId(),
                        EventModule.Support,
                        EventType.Updated,
                        new ArrayList<>()
                ));

                return sla.get();
            }
        }

        return new SLA();
    }

    @PostMapping("/search")
    public List<UniversalSearchModel> searchRequests(@ModelAttribute SingleStringModel stringModel) {
        // init return
        List<UniversalSearchModel> searchReturn = new ArrayList<>();

        List<Ticket> foundTickets = ticketService.searchAllById(stringModel.name());
        for (Ticket ticket : foundTickets) {
            searchReturn.add(new UniversalSearchModel(ticket.getId(), "ticket", BigInteger.valueOf(0), ticket.getId()));
        }

        List<User> foundUsers = userService.searchAllByFullName(stringModel.name());
        for (User user : foundUsers) {
            searchReturn.add(new UniversalSearchModel(user.getFullName(), "user", user.getId(), ""));
        }
        return searchReturn;
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
}
