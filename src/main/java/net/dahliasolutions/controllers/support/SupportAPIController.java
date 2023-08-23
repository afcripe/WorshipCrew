package net.dahliasolutions.controllers.support;

import lombok.RequiredArgsConstructor;
import net.dahliasolutions.models.EventModule;
import net.dahliasolutions.models.EventType;
import net.dahliasolutions.models.Notification;
import net.dahliasolutions.models.NotificationModel;
import net.dahliasolutions.models.records.BigIntegerStringModel;
import net.dahliasolutions.models.records.SingleBigIntegerModel;
import net.dahliasolutions.models.records.SingleIntModel;
import net.dahliasolutions.models.records.SingleStringModel;
import net.dahliasolutions.models.support.SLA;
import net.dahliasolutions.models.support.Ticket;
import net.dahliasolutions.models.support.TicketNotifyTarget;
import net.dahliasolutions.models.support.TicketPriority;
import net.dahliasolutions.models.user.User;
import net.dahliasolutions.services.support.SupportSettingService;
import net.dahliasolutions.services.support.TicketPriorityService;
import net.dahliasolutions.services.support.TicketSLAService;
import net.dahliasolutions.services.user.UserService;
import org.springframework.web.bind.annotation.*;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/support")
@RequiredArgsConstructor
public class SupportAPIController {

    private final TicketPriorityService ticketPriorityService;
    private final UserService userService;
    private final SupportSettingService supportSettingService;
    private final TicketSLAService slaService;

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
}
