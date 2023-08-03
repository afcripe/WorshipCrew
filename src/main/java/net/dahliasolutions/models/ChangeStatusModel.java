package net.dahliasolutions.models;

import java.math.BigInteger;

public record ChangeStatusModel(
        BigInteger requestId,
        String requestStatus,
        String RequestNote) {
}
