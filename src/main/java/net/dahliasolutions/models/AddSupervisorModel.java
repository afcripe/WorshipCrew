package net.dahliasolutions.models;

import java.math.BigInteger;

public record AddSupervisorModel(
        BigInteger requestId,
        BigInteger userId,
        boolean primary) {
}
