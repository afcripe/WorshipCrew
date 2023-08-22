package net.dahliasolutions.models.support;

import lombok.*;

import java.math.BigInteger;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TicketNewModel {

    private String summary;
    private String details;
    private String priority;
    private BigInteger campus;
    private BigInteger department;
    private String image;
}
