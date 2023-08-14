package net.dahliasolutions.models;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.*;

import java.math.BigInteger;

@Getter
@Setter
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class AdminSettings {

    @Id
    private BigInteger id;
    private String companyName;
    private String wikiHome;
    private String portalHome;
    private String storeHome;
    private String documentationHome;
    private boolean restrictStorePosition;
    private boolean restrictStoreDepartment;
}
