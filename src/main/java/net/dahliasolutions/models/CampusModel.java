package net.dahliasolutions.models;

import java.math.BigInteger;

public record CampusModel(
        BigInteger id,
        String name,
        String city,
        boolean hidden,
        BigInteger managerId) {
}
