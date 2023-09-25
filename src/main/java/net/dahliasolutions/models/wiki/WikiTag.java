package net.dahliasolutions.models.wiki;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigInteger;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WikiTag {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @SequenceGenerator(name = "wiki_tag_gen", sequenceName = "wiki_tag_seq", allocationSize = 1)
    private BigInteger id;
    private String name;

}
