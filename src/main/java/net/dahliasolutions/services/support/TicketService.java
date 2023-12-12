package net.dahliasolutions.services.support;

import jakarta.persistence.Tuple;
import lombok.RequiredArgsConstructor;
import net.dahliasolutions.data.TicketRepository;
import net.dahliasolutions.models.department.DepartmentRegional;
import net.dahliasolutions.models.order.OrderItem;
import net.dahliasolutions.models.order.OrderRequest;
import net.dahliasolutions.models.order.OrderStatus;
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

        SLA sla = null;
        if (supportSetting.getDefaultSLAId().intValue() != 0) {
            sla = slaService.findById(supportSetting.getDefaultSLAId()).get();
        }

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
            case SupportAgents:
                agent = null;
                agentList = getAgents();
                break;
            case RegionalDepartmentDirector:
                BigInteger dirId = user.getDepartment().getRegionalDepartment().getDirectorId();
                agent = userService.findById(dirId).get();
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

        // determine if user is agent for note
        boolean noteAgent = false;
        if (agent != null) {
            noteAgent = agent.equals(user);
        }

        Ticket ticket = ticketRepository.save(new Ticket(
                    idGenerator.generate(),
                    LocalDateTime.now(),
                    dueDate,
                    null,
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
        TicketNote note = ticketNoteService.createTicketNote(new TicketNote(null, null, false,
                noteAgent, model.getDetails(), imageList, user, ticket));
        ticket.getNotes().add(note);
        ticketRepository.save(ticket);

        return ticket;
    }

    @Override
    public Optional<Ticket> findById(String id) {
        return ticketRepository.findById(id);
    }

    @Override
    public List<Ticket> findAll() {
        return ticketRepository.findAll();
    }

    @Override
    public List<Ticket> findAllOpen() {
        List<Ticket> tickets = ticketRepository.findAll();
        List<Ticket> openTickets = new ArrayList<>();
        for (Ticket ticket : tickets) {
            if (!ticket.getTicketStatus().equals(TicketStatus.Closed)) {
                openTickets.add(ticket);
            }
        }
        return openTickets;
    }

    @Override
    public List<Ticket> findAllBySla(SLA sla) {
        return ticketRepository.findAllBySla(sla);
    }

    @Override
    public List<Ticket> findAllOpenBySla(SLA sla) {
        List<Ticket> tickets = ticketRepository.findAllBySla(sla);
        List<Ticket> openTickets = new ArrayList<>();
        for (Ticket ticket : tickets) {
            if (!ticket.getTicketStatus().equals(TicketStatus.Closed)) {
                openTickets.add(ticket);
            }
        }
        return openTickets;
    }

    @Override
    public List<Ticket> findAllByUser(User user) {
        return ticketRepository.findAllByUser(user);
    }

    @Override
    public List<Ticket> findAllByUserOpenOnly(User user) {
        return ticketRepository.findAllByUserAndTicketStatusNot(user, TicketStatus.Closed);
    }

    @Override
    public List<Ticket> findAllByUserAndSlaOpenOnly(User user, SLA sla) {
        return ticketRepository.findAllByUserAndSlaAndTicketStatusNot(user, sla, TicketStatus.Closed);
    }

    @Override
    public List<Ticket> findFirst5ByUser(User user) {
        return ticketRepository.findFirst5ByUserOrderByTicketDateDesc(user);
    }

    @Override
    public List<Ticket> findAllByAgent(User user) {
        return ticketRepository.findAllByAgentId(user.getId());
    }

    @Override
    public List<Ticket> findAllByAgentOpenOnly(User user) {
        return ticketRepository.findAllByAgentIdOpenOnly(user.getId());
    }

    @Override
    public List<Ticket> findAllByAgentAndSlaOpenOnly(User user, SLA sla) {
        return ticketRepository.findAllByAgentIdAndSLAOpenOnly(user.getId(), sla.getId());
    }

    @Override
    public List<Ticket> findAllByMentionOpenOnly(User user) {
        List<Tuple> agentList = ticketRepository.findAllMentionsByAgentId(user.getId());
        List<Ticket> ticketList = new ArrayList<>();
        for (Tuple tuple : agentList) {
            String ticketId = tuple.get(0).toString();
            Optional<Ticket> ticket = ticketRepository.findById(ticketId);
            if (ticket.isPresent()) {
                if (!ticket.get().getTicketStatus().equals(TicketStatus.Closed)) {
                    ticketList.add(ticket.get());
                }
            }
        }
        return ticketList;
    }

    @Override
    public List<Ticket> findAllByMentionAndSlaOpenOnly(User user, SLA sla) {
        List<Tuple> agentList = ticketRepository.findAllMentionsByAgentId(user.getId());
        List<Ticket> ticketList = new ArrayList<>();
        for (Tuple tuple : agentList) {
            String ticketId = tuple.get(0).toString();
            Optional<Ticket> ticket = ticketRepository.findById(ticketId);
            if (ticket.isPresent()) {
                if (!ticket.get().getTicketStatus().equals(TicketStatus.Closed)
                        && ticket.get().getSla().getId().equals(sla.getId())) {
                    ticketList.add(ticket.get());
                }
            }
        }
        return ticketList;
    }

    @Override
    public Ticket save(Ticket ticket) {
        return ticketRepository.save(ticket);
    }

    @Override
    public List<Ticket> searchAllById(String searchTerm) {
        return ticketRepository.searchAllById(searchTerm);
    }

    @Override
    public List<Ticket> findAllByCampusAndCycle(BigInteger campusId, LocalDateTime startDate, LocalDateTime endDate) {
        return ticketRepository.findAllByCampusAndCycle(campusId, startDate, endDate);
    }

    @Override
    public List<Ticket> findAllByUserAndCycle(BigInteger userId, LocalDateTime startDate, LocalDateTime endDate) {
        return ticketRepository.findAllByUserAndCycle(userId, startDate, endDate);
    }

    @Override
    public List<Ticket> findAllByAgentAndCycle(BigInteger agentId, LocalDateTime startDate, LocalDateTime endDate) {
        return ticketRepository.findAllByAgentAndCycle(agentId, startDate, endDate);
    }

    @Override
    public List<Ticket> findAllByMentionOpenAndCycle(BigInteger agentId, LocalDateTime startDate, LocalDateTime endDate) {
        List<Tuple> agentList = ticketRepository.findAllMentionsByAgentId(agentId);
        List<Ticket> ticketList = new ArrayList<>();
        for (Tuple tuple : agentList) {
            String ticketId = tuple.get(0).toString();
            Optional<Ticket> ticket = ticketRepository.findAllByIdAndCycle(ticketId, startDate, endDate);
            if (ticket.isPresent()) {
                ticketList.add(ticket.get());
            }
        }
        return ticketList;
    }

    @Override
    public List<Ticket> findAllByDepartmentAndCycle(BigInteger departmentId, LocalDateTime startDate, LocalDateTime endDate) {
        return ticketRepository.findAllByDepartmentAndCycle(departmentId, startDate, endDate);
    }

    @Override
    public List<Ticket> findAllByDepartmentAndCampusAndCycle(BigInteger departmentId, BigInteger campusId, LocalDateTime startDate, LocalDateTime endDate) {
        return ticketRepository.findAllByDepartmentAndCampusAndCycle(departmentId, campusId, startDate, endDate);
    }

    private List<User> getSupervisors() {
        Collection<UserRoles> roles = getSupervisorCollection();
        List<User> userList = new ArrayList<>();
        List<User> allUsers = userService.findAll();

        for (User user : allUsers) {
            for (UserRoles role : user.getUserRoles()) {
                if (roles.contains(role)) {
                    userList.add(user);
                    break;
                }
            }
        }
        return userList;
    }

    private List<User> getAgents() {
        Collection<UserRoles> roles = getAgentCollection();
        List<User> userList = new ArrayList<>();
        List<User> allUsers = userService.findAll();

        for (User user : allUsers) {
            for (UserRoles role : user.getUserRoles()) {
                if (roles.contains(role)) {
                    userList.add(user);
                    break;
                }
            }
        }
        return userList;
    }

    private Collection<UserRoles> getSupervisorCollection() {
        Collection<UserRoles> roles = new ArrayList<>();
        roles.add(rolesService.findByName("SUPPORT_SUPERVISOR").get());
        return roles;
    }

    private Collection<UserRoles> getAgentCollection() {
        Collection<UserRoles> roles = new ArrayList<>();
        roles.add(rolesService.findByName("SUPPORT_AGENT").get());
        return roles;
    }

}
