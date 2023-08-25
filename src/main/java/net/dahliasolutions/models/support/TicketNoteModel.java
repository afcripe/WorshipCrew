package net.dahliasolutions.models.support;

import java.math.BigInteger;

public record TicketNoteModel (
        boolean isPrivate,
        String detail,
        String images,
        String ticketId){
}
