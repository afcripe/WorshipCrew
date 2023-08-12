package net.dahliasolutions.services.support;

import net.dahliasolutions.models.support.SLA;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;

public interface TicketSLAServiceInterface {

    Optional<SLA> findById(BigInteger id);
    Optional<SLA> findHighestSLA();
    List<SLA> findAll();
    SLA save(SLA sla);
    void deleteById(BigInteger id);

}
