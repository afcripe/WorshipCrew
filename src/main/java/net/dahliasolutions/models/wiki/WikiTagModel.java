package net.dahliasolutions.models.wiki;

import java.math.BigInteger;

public record WikiTagModel(
        BigInteger postId,
        String tagName) {
}
