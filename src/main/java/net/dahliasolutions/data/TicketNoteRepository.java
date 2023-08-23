package net.dahliasolutions.data;

import net.dahliasolutions.models.support.TicketNote;
import net.dahliasolutions.models.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.math.BigInteger;
import java.util.List;

public interface TicketNoteRepository extends JpaRepository<TicketNote, BigInteger> {

    List<TicketNote> findByTicketId(String id);
    List<TicketNote> findAllByUser(User user);
}
