package net.dahliasolutions.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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

    @OneToOne
    private User author;

    @Lob
    @Column(name = "body", columnDefinition="BLOB")
    private String body;

    @ManyToMany
    private List<WikiTag> tagList;
}
