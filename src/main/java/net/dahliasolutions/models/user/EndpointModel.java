package net.dahliasolutions.models.user;

import java.math.BigInteger;

public record EndpointModel(
        BigInteger id,
        String name,
        String token
) {
}
