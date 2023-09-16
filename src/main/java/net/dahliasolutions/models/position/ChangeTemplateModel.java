package net.dahliasolutions.models.position;

import java.math.BigInteger;

public record ChangeTemplateModel(
        BigInteger userId,
        BigInteger templateId ) {
}