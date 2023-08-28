package net.dahliasolutions.models.support;

import lombok.*;
import net.dahliasolutions.models.campus.Campus;

import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TicketCampus {

    private Campus campus;
    private List<Ticket> ticketList;
}
