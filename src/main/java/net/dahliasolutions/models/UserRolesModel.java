package net.dahliasolutions.models;

import java.math.BigInteger;

public record UserRolesModel(BigInteger id,
                             String username,
                             String role) {

}
