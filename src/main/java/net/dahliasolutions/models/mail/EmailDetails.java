package net.dahliasolutions.models.mail;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigInteger;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EmailDetails {
    private BigInteger messageId;
    private String recipient;
    private String subject;
    private String msgBody;
    private String attachment;
}
