package net.dahliasolutions.models.store;

import java.math.BigInteger;

public record StoreItemModel(
        BigInteger id,
        String name,
        String specialOrder,
        String available,
        int leadTime,
        String description,
        BigInteger image,
        BigInteger category,
        BigInteger subCategory,
        BigInteger department,
        String position,
        String resourceLink
        ) {
}