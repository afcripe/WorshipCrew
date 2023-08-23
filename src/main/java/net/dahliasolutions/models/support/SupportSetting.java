package net.dahliasolutions.models.support;

import jakarta.annotation.Nullable;
import jakarta.persistence.*;
import lombok.*;
import net.dahliasolutions.models.store.RequestNotifyTarget;
import net.dahliasolutions.models.user.User;

import java.math.BigInteger;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
public class SupportSetting {

    @Id
    private BigInteger id;
    private int responseHours;
    private BigInteger defaultSLAId;

    @Enumerated
    private TicketNotifyTarget notifyTarget;

    @Nullable
    @OneToOne(fetch = FetchType.EAGER)
    private User user;

}
