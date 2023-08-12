package net.dahliasolutions.services.support;

import net.dahliasolutions.models.support.TicketPriority;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;

public interface TicketPriorityServiceInterface {

    TicketPriority createTicket(TicketPriority ticketPriority);
    Optional<TicketPriority> findById(BigInteger id);
    List<TicketPriority> findAll();
    TicketPriority save(TicketPriority ticketPriority);
    void deleteById(BigInteger id);

}
