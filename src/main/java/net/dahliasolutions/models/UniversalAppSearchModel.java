package net.dahliasolutions.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigInteger;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UniversalAppSearchModel {
    private String searchType;
    private String searchId;
    private String searchName;
    private String searchDetail;
}
