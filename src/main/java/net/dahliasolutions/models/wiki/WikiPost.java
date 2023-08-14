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
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private BigInteger id;
    private String title;
    private String folder;
    private LocalDateTime created;
    private LocalDateTime lastUpdated;
    private String summary;
    private boolean anonymous;
    private boolean published;

    @ManyToOne
    private User author;

    @Lob
    @Column(name = "body", columnDefinition="BLOB")
    private String body;

    @ManyToMany
    private List<WikiTag> tagList;
}
