package net.dahliasolutions.models.order;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;
import net.dahliasolutions.models.campus.Campus;
import net.dahliasolutions.models.user.User;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AppRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private BigInteger id;
    private LocalDateTime requestDate;
    private String requestNote;
    transient int itemCount;
    private boolean editable;

    private Campus campus;

    @Enumerated(EnumType.STRING)
    private OrderStatus orderStatus;

    private User user;

    private User supervisor;

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

    public void setAppRequestByRequest(OrderRequest request) {
        this.id = request.getId();
        this.requestDate = request.getRequestDate();
        this.requestNote = request.getRequestNote();
        this.itemCount = request.getItemCount();
        this.setEditable(false);
        this.campus = request.getCampus();
        this.orderStatus = request.getOrderStatus();
        this.user = request.getUser();
        this.supervisor = request.getSupervisor();
        System.out.println();
    }
}
