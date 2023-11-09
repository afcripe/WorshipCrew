package net.dahliasolutions.models.order;

import jakarta.annotation.Nullable;
import jakarta.persistence.*;
import lombok.*;
import net.dahliasolutions.models.user.User;

import java.math.BigInteger;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class OrderNote {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "order_note_gen")
    @SequenceGenerator(name = "order_note_gen", sequenceName = "order_note_seq", allocationSize = 1)
    private BigInteger id;
    private BigInteger orderId;
    private LocalDateTime noteDate;
    private String orderNote;

    @Nullable
    private BigInteger orderItemId;

    @Enumerated(EnumType.STRING)
    private OrderStatus orderStatus;

    @ManyToOne(fetch = FetchType.EAGER)
    private User user;

}
