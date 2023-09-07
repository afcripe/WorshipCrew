package net.dahliasolutions.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigInteger;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AppItem {

    private String id;
    private String name;
    private String detail;
    private LocalDateTime date;
    private int itemCount;
    private String user;
    private String module;


}
