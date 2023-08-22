package net.dahliasolutions.services.support;

import net.dahliasolutions.models.order.OrderRequest;
import net.dahliasolutions.models.store.Cart;
import net.dahliasolutions.models.support.Ticket;
import net.dahliasolutions.models.support.TicketNewModel;
import net.dahliasolutions.models.user.User;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;

public interface TicketServiceInterface {

    Ticket createTicket(TicketNewModel model, User user);
    Optional<Ticket> findById(BigInteger id);
    List<Ticket> findAll();
    List<Ticket> findAllByUser(User user);
    List<Ticket> findFirst5ByUser(User user);
    List<Ticket> findAllByAgent(User user);
    List<Ticket> findAllByAgentOpenOnly(User user);
    List<Ticket> findAllByMentionOpenOnly(User user);
    Ticket save(Ticket ticket);
    void deleteById(BigInteger id);

}
