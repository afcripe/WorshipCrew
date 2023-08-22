package net.dahliasolutions.services.support;

import lombok.RequiredArgsConstructor;
import net.dahliasolutions.data.TicketPriorityRepository;
import net.dahliasolutions.models.support.TicketPriority;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TicketPriorityService implements TicketPriorityServiceInterface {

    private final TicketPriorityRepository ticketPriorityRepository;


    @Override
    public TicketPriority createPriority(TicketPriority ticketPriority) {
        return ticketPriorityRepository.save(ticketPriority);
    }

    @Override
    public int getNextDisplayOrder() {
        List<TicketPriority> lastPriority = ticketPriorityRepository.findFirst1OrderByDisplayOrderDesc();
        if (lastPriority.isEmpty()) {
            return 1;
        }
        return lastPriority.get(0).getDisplayOrder()+1;
    }

    @Override
    public Optional<TicketPriority> findById(BigInteger id) {
        return ticketPriorityRepository.findById(id);
    }

    @Override
    public Optional<TicketPriority> findByPriorityLikeIgnoreCase(String priority) {
        return ticketPriorityRepository.findByPriorityLikeIgnoreCase(priority);
    }

    @Override
    public List<TicketPriority> findAll() {
        return ticketPriorityRepository.findAllOrderByDisplayOrderAsc();
    }

    @Override
    public TicketPriority save(TicketPriority ticketPriority) {
        return ticketPriorityRepository.save(ticketPriority);
    }

    @Override
    public void deleteById(BigInteger id) {
        ticketPriorityRepository.deleteById(id);
    }
}
