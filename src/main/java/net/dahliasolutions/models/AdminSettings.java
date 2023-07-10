package net.dahliasolutions.models;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigInteger;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class AdminSettings {

    @Id
    private BigInteger id;
    private String companyName;
    private boolean monthlyStatements;
    private boolean allowVolunteerRequests;
}
