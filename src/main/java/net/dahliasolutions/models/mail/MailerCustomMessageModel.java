package net.dahliasolutions.models.mail;

import lombok.*;

import java.math.BigInteger;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class MailerCustomMessageModel {

    private BigInteger id;
    private String subject;
    private boolean draft;
    private String messageBody;
    private BigInteger userId;
    private String toUsersId;
    private String toUsersName;
}
