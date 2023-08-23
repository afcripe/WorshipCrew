package net.dahliasolutions.services.support;

import lombok.RequiredArgsConstructor;
import net.dahliasolutions.data.TicketRepository;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.id.Configurable;
import org.hibernate.id.IdentifierGenerator;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.type.Type;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.util.Properties;

@Service
@RequiredArgsConstructor
public class TicketIdGenerator {

    private final SupportSettingService settingService;
    private final TicketRepository ticketRepository;


    public String generate() {
        String prefix = settingService.getIdPrefix();

        int cRecord = ticketRepository.countAllById();
        String sNext = Integer.toString(cRecord+1);
        int cLead = 7-sNext.length();
        for (int i=1; i<cLead; i++) {
            prefix = prefix+"0";
        }

        return prefix + sNext;
    }
}

