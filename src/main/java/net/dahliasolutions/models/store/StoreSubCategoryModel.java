package net.dahliasolutions.models.store;

import java.math.BigInteger;

public record StoreSubCategoryModel(
        BigInteger id,
        String name,
        BigInteger parentId
        ) {
}