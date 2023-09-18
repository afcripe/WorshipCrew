package net.dahliasolutions.models;

public record FirebaseMessageModel (
        String recipientToken,
        String title,
        String body
){

}
