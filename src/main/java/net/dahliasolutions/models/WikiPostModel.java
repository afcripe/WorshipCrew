package net.dahliasolutions.models;

import java.math.BigInteger;

public record WikiPostModel (
        BigInteger id,
        String title,
        String body,
        String folder,
        BigInteger authorId ) {
}
