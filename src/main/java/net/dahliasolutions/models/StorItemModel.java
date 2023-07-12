package net.dahliasolutions.models;

import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;

import java.math.BigInteger;

public record StorItemModel(
        BigInteger id,
        String name,
        String description,
        int count,
        boolean specialOrder,
        boolean available,
        int leadTime,
        BigInteger department,
        BigInteger owner,
        BigInteger image,
        String position
        ) {
}