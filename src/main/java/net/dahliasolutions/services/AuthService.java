package net.dahliasolutions.services;

import lombok.RequiredArgsConstructor;
import net.dahliasolutions.models.LoginModel;
import net.dahliasolutions.models.User;
import net.dahliasolutions.models.UserRoles;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service@RequiredArgsConstructor
public class AuthService {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

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

        return passwordEncoder.matches(password, user.getPassword());
    }

    public String randomStringGenerator(int length, boolean useLetters, boolean useNumbers) {
        return RandomStringUtils.random(length ,useLetters, useNumbers);
    }

}
