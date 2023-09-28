package net.dahliasolutions.services.support;

import net.dahliasolutions.models.order.OrderItem;
import net.dahliasolutions.models.order.OrderRequest;
import net.dahliasolutions.models.store.Cart;
import net.dahliasolutions.models.support.Ticket;
import net.dahliasolutions.models.support.TicketImage;
import net.dahliasolutions.models.support.TicketNewModel;
import net.dahliasolutions.models.user.User;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface TicketServiceInterface {


    Ticket createTicket(TicketNewModel model, User user, TicketImage image);
    Optional<Ticket> findById(String id);
    List<Ticket> findAll();
    List<Ticket> findAllOpen();
    List<Ticket> findAllByUser(User user);
    List<Ticket> findAllByUserOpenOnly(User user);
    List<Ticket> findFirst5ByUser(User user);
    List<Ticket> findAllByAgent(User user);
    List<Ticket> findAllByAgentOpenOnly(User user);
    List<Ticket> findAllByMentionOpenOnly(User user);
    Ticket save(Ticket ticket);
    List<Ticket> searchAllById(String searchTerm);
    List<Ticket> findAllByCampusAndCycle(BigInteger campusId, LocalDateTime startDate, LocalDateTime endDate);
    List<Ticket> findAllByUserAndCycle(BigInteger userId, LocalDateTime startDate, LocalDateTime endDate);
    List<Ticket> findAllByAgentAndCycle(BigInteger agentId, LocalDateTime startDate, LocalDateTime endDate);
    List<Ticket> findAllByMentionOpenAndCycle(BigInteger agentId, LocalDateTime startDate, LocalDateTime endDate);
    List<Ticket> findAllByDepartmentAndCycle(BigInteger departmentId, LocalDateTime startDate, LocalDateTime endDate);
    List<Ticket> findAllByDepartmentAndCampusAndCycle(BigInteger departmentId, BigInteger campusId, LocalDateTime startDate, LocalDateTime endDate);

}
