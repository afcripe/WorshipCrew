package net.dahliasolutions.services.support;

import lombok.RequiredArgsConstructor;
import net.dahliasolutions.data.TicketSLARepository;
import net.dahliasolutions.models.support.SLA;
import net.dahliasolutions.models.support.TicketPriority;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TicketSLAService implements TicketSLAServiceInterface {

    private final TicketSLARepository ticketSLARepository;


    @Override
    public Optional<SLA> findById(BigInteger id) {
        return ticketSLARepository.findById(id);
    }

    @Override
    public Optional<SLA> findHighestSLA() {
        return ticketSLARepository.findFirstOrderByResponseLevelDesc();
    }

    @Override
    public List<SLA> findAll() {
        return ticketSLARepository.findAll();
    }

    @Override
    public SLA save(SLA sla) {
        return ticketSLARepository.save(sla);
    }

    @Override
    public void deleteById(BigInteger id) {
        ticketSLARepository.deleteById(id);
    }
}
