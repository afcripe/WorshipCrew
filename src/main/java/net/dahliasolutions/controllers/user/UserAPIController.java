package net.dahliasolutions.controllers.user;

import lombok.RequiredArgsConstructor;
import net.dahliasolutions.models.records.SingleStringModel;
import net.dahliasolutions.models.campus.Campus;
import net.dahliasolutions.models.user.User;
import net.dahliasolutions.models.user.UserRoles;
import net.dahliasolutions.services.campus.CampusService;
import net.dahliasolutions.services.user.UserRolesService;
import net.dahliasolutions.services.user.UserService;
import org.springframework.web.bind.annotation.*;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
public class UserAPIController {

    private final UserService userService;
    private final UserRolesService userRolesService;
    private final CampusService campusService;

    @GetMapping("")
    public List<User> goUserHome() {
        return userService.findAll();
    }

    @GetMapping ("/campus/{campus}")
    public List<User> getUserList(@PathVariable String campus) {
        Optional<Campus> campusOption = campusService.findByName(campus);
        return userService.findAllByCampus(campusOption.get());
    }

    @PostMapping("/setrole/{id}")
    public String setUserRole(@ModelAttribute SingleStringModel role, @PathVariable BigInteger id) {
        Optional<UserRoles> userRole = userRolesService.findByName(role.name());
        Optional<User> user = userService.findById(id);
        boolean found = false;
        if (userRole.isPresent() && user.isPresent()) {
            Collection<UserRoles> newRoles = new ArrayList<>();
            for (UserRoles r : user.get().getUserRoles()) {
                if (!r.getName().equals(userRole.get().getName())) {
                    newRoles.add(r);
                } else {
                    found = true;
                }
            }
            if (!found) {
                newRoles.add(userRole.get());
            }

            user.get().setUserRoles(newRoles);
            userService.save(user.get());
            return "true";
        }
        return "false";
    }
}
