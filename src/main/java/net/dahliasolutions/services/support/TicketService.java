package net.dahliasolutions.services.support;

import lombok.RequiredArgsConstructor;
import net.dahliasolutions.data.TicketRepository;
import net.dahliasolutions.models.support.SupportSetting;
import net.dahliasolutions.models.support.Ticket;
import net.dahliasolutions.models.support.TicketNewModel;
import net.dahliasolutions.models.support.TicketStatus;
import net.dahliasolutions.models.user.User;
import net.dahliasolutions.services.campus.CampusService;
import net.dahliasolutions.services.department.DepartmentRegionalService;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TicketService implements TicketServiceInterface {

    private final TicketRepository ticketRepository;
    private final CampusService campusService;
    private final DepartmentRegionalService departmentService;
    private final SupportSettingService supportSettingService;


    @Override
    public Ticket createTicket(TicketNewModel model, User user) {
        SupportSetting supportSetting = supportSettingService.getSupportSetting();

        // ToDo - create default respond days in supportSetting
        int defaultDue = 3;
        LocalDateTime today = LocalDateTime.now();
        LocalDateTime dueDate = today.plusDays(defaultDue);

        // ToDo - create default SLA in supportSetting

        // ToDo - determine who gets the ticket base on supportSetting
        User agent = user.getDirector();

        Ticket ticket = ticketRepository.save(new Ticket(
                    null,
                    today,
                    dueDate,
                    model.getSummary(),
                    model.getPriority(),
                    null,
                    TicketStatus.Open,
                    campusService.findById(model.getCampus()).get(),
                    departmentService.findById(model.getDepartment()).get(),
                    user,
                    agent,
                    new ArrayList<>(),
                    new ArrayList<>()
                )
        );

        // ToDo - add new ticket note before returning

        return ticket;
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
