package net.dahliasolutions.models;

import lombok.*;

import java.math.BigInteger;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UniversalSearchModel {
    private String searchTerm;
    private String searchType;
    private BigInteger searchId;
    private String searchStringId;
}
