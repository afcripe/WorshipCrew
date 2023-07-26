package net.dahliasolutions.models.campus;

import java.math.BigInteger;

public record CampusModel(
        BigInteger id,
        String name,
        String city,
        boolean hidden,
        BigInteger directorId) {
}
