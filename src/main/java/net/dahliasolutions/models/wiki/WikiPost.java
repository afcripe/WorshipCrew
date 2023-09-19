package net.dahliasolutions.models.wiki;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.dahliasolutions.models.user.User;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WikiPost {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private BigInteger id;
    private String title;
    private String folder;
    private LocalDateTime created;
    private LocalDateTime lastUpdated;
    private String summary;
    private boolean anonymous;
    private boolean published;
    private boolean hideInfo;

    @ManyToOne
    private User author;

    @Column(name = "body", columnDefinition = "text")
    private String body;

    @ManyToMany
    private List<WikiTag> tagList;
}
