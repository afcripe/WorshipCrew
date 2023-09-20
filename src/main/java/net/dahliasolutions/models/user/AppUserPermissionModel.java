package net.dahliasolutions.models.user;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class AppUserPermissionModel {
    private boolean userMod;
    private boolean requestMod;
    private boolean ticketMod;

    public AppUserPermissionModel() {
        this.userMod = false;
        this.requestMod = false;
        this.ticketMod = false;
    }
}
