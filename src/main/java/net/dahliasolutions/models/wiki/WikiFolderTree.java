package net.dahliasolutions.models.wiki;

import jakarta.persistence.Id;
import lombok.*;

import java.util.List;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class WikiFolderTree {

    @Id
    private String name;
    private String path;
    private List<WikiFolderTree> folders;

}
