package net.dahliasolutions.controllers.support;

import lombok.RequiredArgsConstructor;
import net.dahliasolutions.models.EventModule;
import net.dahliasolutions.models.EventType;
import net.dahliasolutions.models.Notification;
import net.dahliasolutions.models.NotificationModel;
import net.dahliasolutions.models.position.Position;
import net.dahliasolutions.models.records.BigIntegerStringModel;
import net.dahliasolutions.models.records.SingleBigIntegerModel;
import net.dahliasolutions.models.records.SingleIntModel;
import net.dahliasolutions.models.records.SingleStringModel;
import net.dahliasolutions.models.support.*;
import net.dahliasolutions.models.user.User;
import net.dahliasolutions.models.user.UserRoles;
import net.dahliasolutions.services.support.*;
import net.dahliasolutions.services.user.UserService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.math.BigInteger;
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
    public SLA getOrderNotification(@ModelAttribute SingleBigIntegerModel intModel) {
        Optional<SLA> sla = slaService.findById(intModel.id());
        return sla.orElseGet(SLA::new);
    }

    @PostMapping("/sla/update")
    public SLA updateOrderNotification(@ModelAttribute SLA slaModel) {
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
    public SingleBigIntegerModel deleteOrderNotification(@ModelAttribute SingleBigIntegerModel intModel) {
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
        if (currentUser.equals(ticket.get().getUser())) {
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

        return note;
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
}
