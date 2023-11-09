package net.dahliasolutions.models.support;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigInteger;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class TicketImage {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "ticket_image_gen")
    @SequenceGenerator(name = "ticket_image_gen", sequenceName = "ticket_image_seq", allocationSize = 1)
    private BigInteger id;
    private String name;
    private String description;
    private String fileLocation;
}
