package net.dahliasolutions.controllers;

import lombok.RequiredArgsConstructor;
import net.dahliasolutions.models.LoginModel;
import net.dahliasolutions.services.AuthService;
import net.dahliasolutions.services.user.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.web.bind.annotation.*;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthAPIController {

    private final AuthService authService;

    @PostMapping("/login")
    public AuthenticationResponse getAuthUser(@ModelAttribute LoginModel loginModel) {
        System.out.println(loginModel);
        AuthenticationResponse response = authService.authenticate(loginModel);
        return response;
    }

}
