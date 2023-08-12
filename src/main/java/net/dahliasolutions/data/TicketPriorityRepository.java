package net.dahliasolutions.data;

import net.dahliasolutions.models.support.TicketPriority;
import org.springframework.data.jpa.repository.JpaRepository;

import java.math.BigInteger;

public interface TicketPriorityRepository extends JpaRepository<TicketPriority, BigInteger> {

}
