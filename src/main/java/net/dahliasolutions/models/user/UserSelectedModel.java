package net.dahliasolutions.models.user;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigInteger;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserSelectedModel {
    private BigInteger id;
    private String fullName;
    private boolean selected;
}
