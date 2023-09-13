package net.dahliasolutions.models;

import lombok.Data;

import java.util.Map;

@Data
public class FirebaseMessage {
    private String recipientToken;
    private String title;
    private String body;
    private String image;
    private Map<String,String> data;
}
