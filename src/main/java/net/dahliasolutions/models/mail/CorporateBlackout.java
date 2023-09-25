package net.dahliasolutions.models.mail;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigInteger;
import java.time.LocalTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class CorporateBlackout {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @SequenceGenerator(name = "corporate_blackout_generator", sequenceName = "corporate_blackout_seq", allocationSize = 1)
    private BigInteger id;
    private int blackoutYear;
    private int blackoutMonth;
    private int blackoutStart;
    private int blackoutDayEnd;
    private LocalTime timeStart;
    private LocalTime timeEnd;
    private boolean repeats;
    private String repeatCycle;
    private boolean activated;
    private BigInteger userId;
}
