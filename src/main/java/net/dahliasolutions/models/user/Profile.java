package net.dahliasolutions.models.user;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigInteger;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Profile {

    @Id
    @Column(name="id")
    private BigInteger id;
    private String theme;
    private String sideNavigation;
    private String storeLayout;

    @Enumerated
    private NotificationChannel notificationChannel;

    @OneToOne
    private User user;
}
