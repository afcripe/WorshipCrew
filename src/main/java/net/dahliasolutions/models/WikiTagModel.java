package net.dahliasolutions.models;

import java.math.BigInteger;

public record WikiTagModel(
        BigInteger postId,
        String tagName) {
}
