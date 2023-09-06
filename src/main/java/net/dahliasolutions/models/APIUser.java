package net.dahliasolutions.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.dahliasolutions.models.user.User;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class APIUser {
    private boolean valid;
    private User user;
}
