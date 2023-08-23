package net.dahliasolutions.services.support;

import lombok.RequiredArgsConstructor;
import net.dahliasolutions.data.TicketRepository;
import net.dahliasolutions.models.position.Position;
import net.dahliasolutions.models.store.StoreSetting;
import net.dahliasolutions.models.support.*;
import net.dahliasolutions.models.user.User;
import net.dahliasolutions.models.user.UserRoles;
import net.dahliasolutions.services.campus.CampusService;
import net.dahliasolutions.services.department.DepartmentRegionalService;
import net.dahliasolutions.services.user.UserRolesService;
import net.dahliasolutions.services.user.UserService;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TicketService implements TicketServiceInterface {

    private final TicketRepository ticketRepository;
    private final CampusService campusService;
    private final DepartmentRegionalService departmentService;
    private final SupportSettingService supportSettingService;
    private final UserService userService;
    private final UserRolesService rolesService;
    private final TicketSLAService slaService;
    private final TicketNoteService ticketNoteService;
    private final TicketIdGenerator idGenerator;

    @Override
    public Ticket createTicket(TicketNewModel model, User user, TicketImage image) {
        SupportSetting supportSetting = supportSettingService.getSupportSetting();

        // create default respond days in supportSetting
        LocalDateTime dueDate = LocalDateTime.now().plusHours(supportSetting.getResponseHours());

        // ToDo - create default SLA in supportSetting
        SLA sla = null;
        if (supportSetting.getDefaultSLAId().intValue() != 0) {
            sla = slaService.findById(supportSetting.getDefaultSLAId()).get();
        }

        // ToDo - determine who gets the ticket base on supportSetting
        User agent;
        List<User> agentList = new ArrayList<>();
        switch (supportSetting.getNotifyTarget()) {
            case User:
                if (supportSetting.getUser() != null) {
                    agent = supportSetting.getUser();
                    break;
                }
            case SupportSupervisors:
                agent = null;
                agentList = getSupervisors();
                break;
            case RegionalDepartmentDirector:
                agent = userService.findById(user.getDepartment().getRegionalDepartment().getDirectorId()).get();
                break;
            case CampusDepartmentDirector:
                agent = userService.findById(user.getDepartment().getDirectorId()).get();
                break;
            case CampusDirector:
                agent = userService.findById(user.getCampus().getDirectorId()).get();
                break;
            default:
                agent = user.getDirector();
                break;
        }

        Ticket ticket = ticketRepository.save(new Ticket(
                    idGenerator.generate(),
                    LocalDateTime.now(),
                    dueDate,
                    model.getSummary(),
                    model.getPriority(),
                    sla,
                    TicketStatus.Open,
                    campusService.findById(model.getCampus()).get(),
                    departmentService.findById(model.getDepartment()).get(),
                    user,
                    agent,
                    new ArrayList<>(),
                    agentList
                )
        );

        // ticketImages
        List<TicketImage> imageList = new ArrayList<>();
        if (image != null) {
            imageList.add(image);
        }

        // add first note
        TicketNote note = ticketNoteService.createTicketNote(new TicketNote(null, null, true,
                TicketStatus.Open, model.getDetails(), imageList, user, agent, ticket));
        ticket.getNotes().add(note);

        return ticket;
    }

    @Override
    public Optional<Ticket> findById(String id) {
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

    private List<User> getSupervisors() {
        Optional<UserRoles> role = rolesService.findByName("SUPPORT_SUPERVISOR");
        if (role.isPresent()) {
            return userService.findAllByRole(role.get());
        }
        return new ArrayList<>();
    }

}
