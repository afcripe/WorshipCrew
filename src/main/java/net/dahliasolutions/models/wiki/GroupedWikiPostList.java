package net.dahliasolutions.models.wiki;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.dahliasolutions.models.wiki.WikiPost;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GroupedWikiPostList {

    private String folder;
    private List<WikiPost> wikiPost;
    private int postCount;
    private List<WikiFolder> subFolders;
}
