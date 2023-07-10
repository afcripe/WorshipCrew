package net.dahliasolutions.models;

import java.math.BigInteger;

public record UserModel (BigInteger id,
                         String username,
                         String password,
                         String firstName,
                         String lastName,
                         String contactEmail,
                         String position,
                         String department,
                         String campus,
                         String activated) {

}
