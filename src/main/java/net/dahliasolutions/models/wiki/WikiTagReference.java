package net.dahliasolutions.models.wiki;

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
public class WikiTagReference {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "wiki_tag_reference_gen")
    @SequenceGenerator(name = "wiki_tag_reference_gen", sequenceName = "wiki_tag_reference_seq", allocationSize = 1)
    private BigInteger id;
    private String name;
    private int referencedTag;

}
