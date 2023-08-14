package net.dahliasolutions.models.store;

import jakarta.annotation.Nullable;
import jakarta.persistence.*;
import lombok.*;
import net.dahliasolutions.models.user.User;

import java.math.BigInteger;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
public class StoreSetting {

    @Id
    private BigInteger id;

    @Enumerated
    private StoreNotifyTarget notifyTarget;

    @Nullable
    @OneToOne(fetch = FetchType.EAGER)
    private User user;

}
