package net.dahliasolutions.models;

import java.math.BigInteger;

public record StoreItemModel(
        BigInteger id,
        String name,
        String description,
        int count,
        String specialOrder,
        String available,
        int leadTime,
        BigInteger department,
        BigInteger owner,
        BigInteger image,
        String position
        ) {
}