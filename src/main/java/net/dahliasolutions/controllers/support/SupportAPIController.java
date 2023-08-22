package net.dahliasolutions.controllers.support;

import lombok.RequiredArgsConstructor;
import net.dahliasolutions.models.records.SingleBigIntegerModel;
import net.dahliasolutions.models.records.SingleIntModel;
import net.dahliasolutions.models.records.SingleStringModel;
import net.dahliasolutions.models.store.StoreCategory;
import net.dahliasolutions.models.support.Ticket;
import net.dahliasolutions.models.support.TicketPriority;
import net.dahliasolutions.services.support.TicketPriorityService;
import org.springframework.web.bind.annotation.*;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/support")
@RequiredArgsConstructor
public class SupportAPIController {

    private final TicketPriorityService ticketPriorityService;

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
}
