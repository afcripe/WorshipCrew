package net.dahliasolutions.services.support;

import lombok.RequiredArgsConstructor;
import net.dahliasolutions.data.TicketNoteRepository;
import net.dahliasolutions.data.TicketNoteRepository;
import net.dahliasolutions.models.support.TicketNote;
import net.dahliasolutions.models.support.TicketNote;
import net.dahliasolutions.services.support.TicketNoteServiceInterface;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TicketNoteService implements TicketNoteServiceInterface {

    private final TicketNoteRepository ticketNoteRepository;


    @Override
    public TicketNote createTicketNote(TicketNote ticketNote) {
        ticketNote.setNoteDate(LocalDateTime.now());
        return ticketNoteRepository.save(ticketNote);
    }

    @Override
    public Optional<TicketNote> findById(BigInteger id) {
        return ticketNoteRepository.findById(id);
    }

    @Override
    public List<TicketNote> findAll() {
        return ticketNoteRepository.findAll();
    }

    @Override
    public List<TicketNote> findByTicketId(String ticketId) {
        return ticketNoteRepository.findByTicketId(ticketId);
    }

    @Override
    public void save(TicketNote ticketNote) {
        ticketNoteRepository.save(ticketNote);
    }

    @Override
    public void deleteById(BigInteger id) {
        ticketNoteRepository.deleteById(id);
    }
}
