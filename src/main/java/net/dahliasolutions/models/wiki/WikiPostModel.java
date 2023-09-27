package net.dahliasolutions.models.wiki;

import java.math.BigInteger;

public record WikiPostModel (
        BigInteger id,
        String title,
        String body,
        String folder,
        String summary,
        BigInteger authorId,
        String anonymous,
        String published,
        String hideInfo,
        String pinToTop) {
}
