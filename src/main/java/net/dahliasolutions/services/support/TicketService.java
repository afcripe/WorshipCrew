package net.dahliasolutions.services.support;

import lombok.RequiredArgsConstructor;
import net.dahliasolutions.data.TicketRepository;
import net.dahliasolutions.models.support.Ticket;
import net.dahliasolutions.models.user.User;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TicketService implements TicketServiceInterface {

    private final TicketRepository ticketRepository;


    @Override
    public Ticket createTicket(Ticket ticket) {
        return null;
    }

    @Override
    public Optional<Ticket> findById(BigInteger id) {
        return Optional.empty();
    }

    @Override
    public List<Ticket> findAll() {
        return null;
    }

    @Override
    public List<Ticket> findAllByUser(User user) {
        return null;
    }

    @Override
    public List<Ticket> findFirst5ByUser(User user) {
        return null;
    }

    @Override
    public List<Ticket> findAllByAgent(User user) {
        return null;
    }

    @Override
    public List<Ticket> findAllByAgentOpenOnly(User user) {
        return null;
    }

    @Override
    public List<Ticket> findAllByMentionOpenOnly(User user) {
        return null;
    }

    @Override
    public Ticket save(Ticket ticket) {
        return null;
    }

    @Override
    public void deleteById(BigInteger id) {

    }
}
