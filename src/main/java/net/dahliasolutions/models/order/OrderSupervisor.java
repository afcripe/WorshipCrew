package net.dahliasolutions.models.order;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigInteger;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderSupervisor {
    private BigInteger orderRequestId;
    private BigInteger supervisorListId;
}
