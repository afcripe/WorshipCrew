package net.dahliasolutions.models.support;

import java.math.BigInteger;

public record TicketNoteEditModel(
        BigInteger id,
        boolean isPrivate,
        String detail){
}
