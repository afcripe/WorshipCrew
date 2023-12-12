package net.dahliasolutions.models.records;

import java.math.BigInteger;

public record ChangeStatusModel(
        BigInteger requestId,
        String requestStatus,
        String requestNote,
        boolean items) {
}
