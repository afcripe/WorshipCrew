package net.dahliasolutions.models.wiki;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigInteger;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class WikiImageManager {

    private BigInteger id;
    private WikiImageModel image;
    private List<WikiPost> items;
}
