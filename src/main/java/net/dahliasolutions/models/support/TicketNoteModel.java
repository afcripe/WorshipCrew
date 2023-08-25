package net.dahliasolutions.models.support;

import java.math.BigInteger;

public record TicketNoteModel (
        String isPrivate,
        String detail,
        String images,
        String ticketId){
}
