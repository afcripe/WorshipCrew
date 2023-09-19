package net.dahliasolutions.models.order;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;
import net.dahliasolutions.models.campus.Campus;
import net.dahliasolutions.models.department.DepartmentRegional;
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
public class OrderRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private BigInteger id;
    private LocalDateTime requestDate;
    private String requestNote;
    transient int itemCount;

    @ManyToOne(fetch = FetchType.EAGER)
    private Campus campus;

    @Enumerated(EnumType.STRING)
    private OrderStatus orderStatus;

    @ManyToOne(fetch = FetchType.EAGER)
    private User user;

    @ManyToOne(fetch = FetchType.EAGER)
    private User supervisor;

    @JsonBackReference
    @OneToMany(fetch = FetchType.EAGER, mappedBy = "orderRequest")
    private List<OrderItem> requestItems;

    @JsonBackReference
    @ManyToMany(fetch = FetchType.EAGER)
    private List<User> supervisorList;

    public int getItemCount() {
        int counter = 0;
        for (OrderItem item : requestItems) {
            counter = counter+item.getCount();
        }
        return counter;
    }

    @Override
    public String toString() {
        return "OrderRequest{" +
                "id=" + id +
                ", requestDate=" + requestDate +
                ", requestNote='" + requestNote + '\'' +
                ", itemCount=" + itemCount +
                ", orderStatus=" + orderStatus +
                '}';
    }
}
