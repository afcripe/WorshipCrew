package net.dahliasolutions.models.order;

import lombok.*;

import java.math.BigInteger;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderSupervisor {
    private BigInteger orderRequestId;
    private BigInteger supervisorListId;
}
