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
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "ticket_note_gen")
    @SequenceGenerator(name = "ticket_note_gen", sequenceName = "ticket_note_seq", allocationSize = 1)
    private BigInteger id;
    private LocalDateTime noteDate;
    private boolean notePrivate;
    private boolean agentNote;

    @Column(name = "detail", columnDefinition = "text")
    private String detail;

    @OneToMany(fetch = FetchType.EAGER)
    private List<TicketImage> images;

    @ManyToOne(fetch = FetchType.EAGER)
    private User user;

    @ManyToOne
    @JoinColumn(name = "ticketId")
    private Ticket ticket;

}
