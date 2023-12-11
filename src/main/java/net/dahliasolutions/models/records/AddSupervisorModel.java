package net.dahliasolutions.models.records;

import java.math.BigInteger;

public record AddSupervisorModel(
        BigInteger requestId,
        BigInteger userId,
        boolean primary,
        boolean items) {
}
