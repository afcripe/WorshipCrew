package net.dahliasolutions.models;

import java.math.BigInteger;

public record CartItemModel(
        BigInteger id,
        BigInteger userId,
        int count

) {
}
