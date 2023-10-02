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
public class WikiNavigator {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @SequenceGenerator(name = "wiki_navigator_gen", sequenceName = "wiki_navigator_seq", allocationSize = 1)
    private Integer id;
    private Integer itemOrder;
    private String name;
    private String linkLocation;
    private Boolean indentationItem;
}
