package net.dahliasolutions.models;

import java.math.BigInteger;

public record FirebaseMessageModel (
        String recipientToken,
        String title,
        String body
){

}
