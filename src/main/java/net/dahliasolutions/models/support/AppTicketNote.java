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
public class AppTicketNote {

    private BigInteger id;
    private LocalDateTime noteDate;
    private boolean notePrivate;
    private boolean agentNote;
    private String user;
    private String detail;

    @OneToMany(fetch = FetchType.LAZY)
    private List<TicketImage> images;

}
