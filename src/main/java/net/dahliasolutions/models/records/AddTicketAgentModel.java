package net.dahliasolutions.models.records;

import java.math.BigInteger;

public record AddTicketAgentModel(
        String id,
        BigInteger userId,
        boolean primary) {
}
