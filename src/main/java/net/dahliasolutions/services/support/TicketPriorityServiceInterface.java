package net.dahliasolutions.services.support;

import net.dahliasolutions.models.support.TicketPriority;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;

public interface TicketPriorityServiceInterface {

    TicketPriority createPriority(TicketPriority ticketPriority);
    int getNextDisplayOrder();
    Optional<TicketPriority> findById(BigInteger id);
    Optional<TicketPriority> findByPriorityLikeIgnoreCase(String priority);
    List<TicketPriority> findAll();
    TicketPriority save(TicketPriority ticketPriority);
    void deleteById(BigInteger id);

}
