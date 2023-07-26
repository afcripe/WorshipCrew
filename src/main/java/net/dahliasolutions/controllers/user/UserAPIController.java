package net.dahliasolutions.controllers.user;

import lombok.RequiredArgsConstructor;
import net.dahliasolutions.models.campus.Campus;
import net.dahliasolutions.models.user.User;
import net.dahliasolutions.services.campus.CampusService;
import net.dahliasolutions.services.user.UserService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
public class UserAPIController {

    private final UserService userService;
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
}
