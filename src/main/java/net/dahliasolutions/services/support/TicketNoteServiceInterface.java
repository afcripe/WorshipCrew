package net.dahliasolutions.services.support;

import net.dahliasolutions.models.support.TicketNote;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;

public interface TicketNoteServiceInterface {

    TicketNote createTicketNote(TicketNote note);
    Optional<TicketNote> findById(BigInteger id);
    List<TicketNote> findAll();
    List<TicketNote> findByTicketId(String ticketId);
    void save(TicketNote note);
    void deleteById(BigInteger id);

}
