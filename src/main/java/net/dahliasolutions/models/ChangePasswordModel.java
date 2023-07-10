package net.dahliasolutions.models;

import java.math.BigInteger;

public record ChangePasswordModel(BigInteger id,
                                  String currentPassword,
                                  String newPassword,
                                  String confirmPassword) {

}
