package net.dahliasolutions.data;

import jakarta.persistence.Tuple;
import net.dahliasolutions.models.campus.Campus;
import net.dahliasolutions.models.department.DepartmentRegional;
import net.dahliasolutions.models.order.OrderRequest;
import net.dahliasolutions.models.support.Ticket;
import net.dahliasolutions.models.support.TicketStatus;
import net.dahliasolutions.models.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;

public interface TicketRepository extends JpaRepository<Ticket, BigInteger> {


    Optional<Ticket> findById(String id);
    List<Ticket> findAllByUserAndTicketStatusNot(User user, TicketStatus status);
    List<Ticket> findAllByUser(User user);
    List<Ticket> findAllByCampusAndTicketStatusNot(Campus campus, TicketStatus status);
    List<Ticket> findAllByCampus(Campus campus);
    List<Ticket> findAllByDepartmentAndTicketStatusNot(DepartmentRegional department, TicketStatus status);
    List<Ticket> findAllByDepartment(DepartmentRegional department);
    List<Ticket> findFirst5ByUserOrderByTicketDateDesc(User user);

    @Query(value = "SELECT COUNT(ID) AS COUNT FROM TICKET", nativeQuery = true)
    int countAllById();
    @Query(value = "SELECT * FROM TICKET WHERE AGENT_ID = :agentId ORDER BY TICKET_DATE DESC", nativeQuery = true)
    List<Ticket> findAllByAgentId(@Param("agentId") BigInteger agentId);
    @Query(value = "SELECT * FROM TICKET WHERE AGENT_ID = :agentId AND TICKET_STATUS <> 'Closed' ORDER BY TICKET_DATE DESC", nativeQuery = true)
    List<Ticket> findAllByAgentIdOpenOnly(@Param("agentId") BigInteger agentId);
    @Query(value = "SELECT * FROM TICKET_AGENT_LIST WHERE AGENT_LIST_ID = :agentId", nativeQuery = true)
    List<Tuple> findAllMentionsByAgentId(@Param("agentId") BigInteger agentId);

}
