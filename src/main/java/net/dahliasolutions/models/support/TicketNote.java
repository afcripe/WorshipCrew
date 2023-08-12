package net.dahliasolutions.models.support;

import jakarta.persistence.*;
import lombok.*;
import net.dahliasolutions.models.user.User;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class TicketNote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private BigInteger id;
    private LocalDateTime noteDate;
    private boolean notePublic;

    @Enumerated(EnumType.STRING)
    private TicketStatus ticketStatus;

    @Lob
    @Column(name = "detail", columnDefinition="BLOB")
    private String detail;

    @OneToMany(fetch = FetchType.LAZY)
    private List<TicketImage> images;

    @ManyToOne(fetch = FetchType.EAGER)
    private User user;

    @ManyToOne(fetch = FetchType.EAGER)
    private User agent;

    @ManyToOne
    @JoinColumn(name = "ticketId")
    private Ticket ticket;

}
