package net.dahliasolutions.models.wiki;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigInteger;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class WikiImage {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @SequenceGenerator(name = "wiki_image_gen", sequenceName = "wiki_image_seq", allocationSize = 1)
    private BigInteger id;
    private String name;
    private String description;
    private String fileLocation;
}
