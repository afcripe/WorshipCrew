package net.dahliasolutions.models.position;

import java.math.BigInteger;

public record PermissionTemplateModel(
        BigInteger id,
        String name,
        String defaultTemplate,
        BigInteger positionId,
        String roles ) {
}