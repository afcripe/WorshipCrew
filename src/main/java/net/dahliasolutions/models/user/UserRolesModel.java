package net.dahliasolutions.models.user;

import java.math.BigInteger;

public record UserRolesModel(BigInteger id,
                             String username,
                             String role) {

}
