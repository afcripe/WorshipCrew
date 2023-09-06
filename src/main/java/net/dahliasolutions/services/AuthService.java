package net.dahliasolutions.services;

import lombok.RequiredArgsConstructor;
import net.dahliasolutions.controllers.AuthenticationResponse;
import net.dahliasolutions.models.LoginModel;
import net.dahliasolutions.models.user.User;
import net.dahliasolutions.models.user.UserRoles;
import net.dahliasolutions.services.user.UserService;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service@RequiredArgsConstructor
public class AuthService {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private  final JwtService jwtService;

    public boolean isAuthorized(String username, String requiredAuth) {
        User user = userService.findByUsername(username).orElse(null);
        if(user != null) {
            for(UserRoles role : user.getUserRoles()){
                if(role.getName().equals(requiredAuth)) {return true;}
            }
        }
        return false;
    }

    public boolean verifyAuthorizedByPassword(LoginModel loginModel, String requiredAuth) {
        User user = userService.findByUsername(loginModel.getUsername()).orElse(null);
        if(user != null) {
            if(passwordEncoder.matches(loginModel.getPassword(), user.getPassword())) {
                for(UserRoles role : user.getUserRoles()){
                    if(role.getName().equals(requiredAuth)) {return true;}
                }
            }
        }
        return false;
    }

    public boolean verifyUserPassword(User user, String password) {
        boolean match = passwordEncoder.matches(password, user.getPassword());
        return passwordEncoder.matches(password, user.getPassword());
    }

    public String randomStringGenerator(int length, boolean useLetters, boolean useNumbers) {
        return RandomStringUtils.random(length ,useLetters, useNumbers);
    }

    public AuthenticationResponse authenticate(LoginModel loginModel) {
        Optional<User> user = userService.findByUsername(loginModel.getUsername());
        if (user.isPresent()) {
            boolean success = verifyUserPassword(user.get(), loginModel.getPassword());
            if (verifyUserPassword(user.get(), loginModel.getPassword())) {
                var jwtToken = jwtService.generateToken(user.get());

                return AuthenticationResponse.builder()
                        .token(jwtToken)
                        .firstName(user.get().getFirstName())
                        .lastName(user.get().getLastName())
                        .loggedIn(true)
                        .build();
            }
        }
        return AuthenticationResponse.builder()
                .token("")
                .firstName("")
                .lastName("")
                .loggedIn(false)
                .build();
    }

    public AuthenticationResponse renewAuth(LoginModel loginModel) {
        Optional<User> user = userService.findByUsername(loginModel.getUsername());
        if (user.isPresent()) {
            var jwtToken = jwtService.generateToken(user.get());
            return AuthenticationResponse.builder()
                    .token(jwtToken)
                    .firstName(user.get().getFirstName())
                    .lastName(user.get().getLastName())
                    .loggedIn(true)
                    .build();
        }
        return AuthenticationResponse.builder()
                .token("")
                .firstName("")
                .lastName("")
                .loggedIn(false)
                .build();
    }

}
