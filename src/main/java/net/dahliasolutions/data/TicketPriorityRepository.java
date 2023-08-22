package net.dahliasolutions.data;

import net.dahliasolutions.models.order.OrderRequest;
import net.dahliasolutions.models.support.TicketPriority;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;

public interface TicketPriorityRepository extends JpaRepository<TicketPriority, BigInteger> {

    Optional<TicketPriority> findByPriorityLikeIgnoreCase(String priority);

    @Query(value = "SELECT * FROM TICKET_PRIORITY ORDER BY DISPLAY_ORDER ASC", nativeQuery = true)
    List<TicketPriority> findAllOrderByDisplayOrderAsc();

    @Query(value = "SELECT * FROM TICKET_PRIORITY ORDER BY DISPLAY_ORDER DESC LIMIT 1", nativeQuery = true)
    List<TicketPriority> findFirst1OrderByDisplayOrderDesc();

}
