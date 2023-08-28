package net.dahliasolutions.models.support;

import lombok.*;
import net.dahliasolutions.models.department.DepartmentRegional;

import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TicketDepartment {

    private DepartmentRegional department;
    private List<Ticket> ticketList;

}
