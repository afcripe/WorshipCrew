package net.dahliasolutions.models;

import java.math.BigInteger;

public record NotificationModel(
        BigInteger id,
        String name,
        String description,
        String module,
        String type,
        String users) {
}