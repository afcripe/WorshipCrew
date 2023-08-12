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

    private final TicketPriorityRepository TicketPriorityRepository;


    @Override
    public TicketPriority createTicket(TicketPriority ticketPriority) {
        return null;
    }

    @Override
    public Optional<TicketPriority> findById(BigInteger id) {
        return Optional.empty();
    }

    @Override
    public List<TicketPriority> findAll() {
        return null;
    }

    @Override
    public TicketPriority save(TicketPriority ticketPriority) {
        return null;
    }

    @Override
    public void deleteById(BigInteger id) {

    }
}
