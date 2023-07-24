package net.dahliasolutions.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigInteger;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WikiFolderReference {

    @Id
    @Column(name = "folder")
    private String folder;
    private int references;

}
