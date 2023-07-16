package net.dahliasolutions.models;

import jakarta.annotation.Nullable;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigInteger;
import java.util.Collection;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class StoreItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private BigInteger id;
    private String name;
    private String description;
    private int count;              //number of items on hand to send to campus
    private boolean specialOrder;   //not held in stock and must be ordered
    private boolean available;      //available to order
    private int leadTime;           //in days

    @Nullable
    @ManyToOne(fetch = FetchType.EAGER)
    private Department department;  //department to which item belongs

    @Nullable
    @ManyToOne(fetch = FetchType.EAGER)
    private User owner;             //person who receives order requests

    @Nullable
    @ManyToOne(fetch = FetchType.LAZY)
    private StoredImage image;

    @ManyToMany(fetch = FetchType.EAGER)
    private Collection<Position> positionList;  //positions allowed to order item

}
