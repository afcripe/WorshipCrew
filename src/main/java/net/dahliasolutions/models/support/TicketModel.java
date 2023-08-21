package net.dahliasolutions.models.support;

import lombok.*;

import java.math.BigInteger;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TicketModel {

    private String detail;
    private String priority;
    private String status;
    private BigInteger campus;
    private BigInteger department;
    private String note;
    private String images;
}
