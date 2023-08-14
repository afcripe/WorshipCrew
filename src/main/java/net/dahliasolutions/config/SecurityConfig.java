package net.dahliasolutions.config;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import net.dahliasolutions.data.UserRepository;
import net.dahliasolutions.models.user.Profile;
import net.dahliasolutions.models.user.User;
import net.dahliasolutions.services.user.ProfileService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import java.util.Optional;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final UserRepository userRepository;
    private final ProfileService profileService;

    @Bean
    public UserDetailsService userDetailsService() {
        return username -> userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Username Not Found."));
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvide = new DaoAuthenticationProvider();
        authProvide.setUserDetailsService(userDetailsService());
        authProvide.setPasswordEncoder(passwordEncoder());
        return authProvide;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http.csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> {
                    auth.requestMatchers("/").permitAll();
                    auth.requestMatchers("/login").permitAll();
                    auth.requestMatchers("/forgotpassword").permitAll();
                    auth.requestMatchers("/signin").permitAll();
                    auth.requestMatchers("/login").permitAll();
                    auth.requestMatchers("/logout").permitAll();
                    auth.requestMatchers("/passwordreset").permitAll();
                    auth.requestMatchers("/logout").permitAll();
                    auth.requestMatchers("/mailer").permitAll();
                    auth.requestMatchers("/mailer/*").permitAll();

                    auth.requestMatchers("/css/**").permitAll();
                    auth.requestMatchers("/img/**").permitAll();
                    auth.requestMatchers("/content/**").permitAll();
                    auth.requestMatchers("/fonts/**").permitAll();
                    auth.requestMatchers("/js/**").permitAll();

                    auth.requestMatchers("/store").hasAnyAuthority("ADMIN_WRITE", "STORE_READ","STORE_WRITE");
                    auth.requestMatchers("/store/item/**").hasAnyAuthority("ADMIN_WRITE", "STORE_READ","STORE_WRITE");
                    auth.requestMatchers("/store/edit/**").hasAnyAuthority("ADMIN_WRITE", "STORE_WRITE");
                    auth.requestMatchers("/store/settings/**").hasAnyAuthority("ADMIN_WRITE");

                    auth.requestMatchers("/request").hasAnyAuthority("ADMIN_WRITE", "REQUEST_WRITE", "REQUEST_SUPERVISOR");
                    auth.requestMatchers("/request/order/**").hasAnyAuthority("ADMIN_WRITE", "REQUEST_WRITE", "REQUEST_SUPERVISOR");
                    auth.requestMatchers("/request/department/**").hasAnyAuthority("ADMIN_WRITE", "REQUEST_SUPERVISOR");
                    auth.requestMatchers("/request/campus/**").hasAnyAuthority("ADMIN_WRITE", "REQUEST_SUPERVISOR");
                    auth.requestMatchers("/request/user/**").hasAnyAuthority("ADMIN_WRITE", "REQUEST_SUPERVISOR");
                    auth.requestMatchers("/request/settings/**").hasAnyAuthority("ADMIN_WRITE");

                    auth.requestMatchers("/resource").hasAnyAuthority("ADMIN_WRITE", "RESOURCE_READ", "RESOURCE_WRITE");
                    auth.requestMatchers("/resource/tag/**").hasAnyAuthority("ADMIN_WRITE", "RESOURCE_READ", "RESOURCE_WRITE");
                    auth.requestMatchers("/resource/folder/**").hasAnyAuthority("ADMIN_WRITE", "RESOURCE_READ", "RESOURCE_WRITE");
                    auth.requestMatchers("/resource/group/**").hasAnyAuthority("ADMIN_WRITE", "RESOURCE_READ", "RESOURCE_WRITE");
                    auth.requestMatchers("/resource/search/**").hasAnyAuthority("ADMIN_WRITE", "RESOURCE_READ", "RESOURCE_WRITE");
                    auth.requestMatchers("/resource/article/**").hasAnyAuthority("ADMIN_WRITE", "RESOURCE_READ", "RESOURCE_WRITE");
                    auth.requestMatchers("/resource/new/**").hasAnyAuthority("ADMIN_WRITE", "RESOURCE_WRITE");
                    auth.requestMatchers("/resource/edit/**").hasAnyAuthority("ADMIN_WRITE", "RESOURCE_WRITE");
                    auth.requestMatchers("/resource/settings").hasAnyAuthority("ADMIN_WRITE");
                    auth.requestMatchers("/resource/tagmanager").hasAnyAuthority("ADMIN_WRITE");
                    auth.requestMatchers("/resource/foldermanager").hasAnyAuthority("ADMIN_WRITE");

                    auth.requestMatchers("/campus").hasAnyAuthority("ADMIN_WRITE", "DIRECTOR_READ", "DIRECTOR_WRITE", "CAMPUS_WRITE", "CAMPUS_READ");
                    auth.requestMatchers("/campus/*").hasAnyAuthority("ADMIN_WRITE", "DIRECTOR_READ", "DIRECTOR_WRITE", "CAMPUS_WRITE", "CAMPUS_READ");
                    auth.requestMatchers("/campus/campus/**").hasAnyAuthority("ADMIN_WRITE", "DIRECTOR_READ", "DIRECTOR_WRITE", "CAMPUS_WRITE", "CAMPUS_READ");
                    auth.requestMatchers("/campus/showhidden").hasAnyAuthority("ADMIN_WRITE");
                    auth.requestMatchers("/campus/new/**").hasAnyAuthority("ADMIN_WRITE");
                    auth.requestMatchers("/campus/create/**").hasAnyAuthority("ADMIN_WRITE");
                    auth.requestMatchers("/campus/edit/**").hasAnyAuthority("ADMIN_WRITE");
                    auth.requestMatchers("/campus/update/**").hasAnyAuthority("ADMIN_WRITE");

                    auth.requestMatchers("/department").hasAnyAuthority("ADMIN_WRITE", "DIRECTOR_READ", "DIRECTOR_WRITE");
                    auth.requestMatchers("/department/*").hasAnyAuthority("ADMIN_WRITE", "DIRECTOR_READ", "DIRECTOR_WRITE");
                    auth.requestMatchers("/department/edit/**").hasAnyAuthority("ADMIN_WRITE");
                    auth.requestMatchers("/department/new/**").hasAnyAuthority("ADMIN_WRITE");
                    auth.requestMatchers("/department/create/**").hasAnyAuthority("ADMIN_WRITE");
                    auth.requestMatchers("/department/update/**").hasAnyAuthority("ADMIN_WRITE");
                    auth.requestMatchers("/department/delete/**").hasAnyAuthority("ADMIN_WRITE");

                    auth.requestMatchers("/position").hasAnyAuthority("ADMIN_WRITE");
                    auth.requestMatchers("/position/**").hasAnyAuthority("ADMIN_WRITE");
                    auth.requestMatchers("/permissiontemplate").hasAnyAuthority("ADMIN_WRITE");
                    auth.requestMatchers("/permissiontemplate/**").hasAnyAuthority("ADMIN_WRITE");

                    auth.requestMatchers("/user").hasAnyAuthority("ADMIN_WRITE", "DIRECTOR_READ", "DIRECTOR_WRITE", "CAMPUS_WRITE", "CAMPUS_READ", "USER_READ", "USER_WRITE");
                    auth.requestMatchers("/user/**").hasAnyAuthority("ADMIN_WRITE", "DIRECTOR_READ", "DIRECTOR_WRITE", "CAMPUS_WRITE", "CAMPUS_READ", "USER_READ", "USER_WRITE");

                    auth.requestMatchers("/admin/**").hasAnyAuthority("ADMIN_WRITE", "DIRECTOR_READ", "DIRECTOR_WRITE", "CAMPUS_WRITE", "CAMPUS_READ", "USER_READ", "USER_WRITE");
                    auth.requestMatchers("/api/v1/**").permitAll();
                    auth.anyRequest().authenticated();
                })
                .formLogin().loginPage("/login").defaultSuccessUrl("/user/").successHandler((request, response, authentication) -> {
                    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
                    User user = (User) auth.getPrincipal();
                    HttpSession session = request.getSession();
                    session.setAttribute("userDisplayName", user.getFirstName());
                    // set Profile Info
                    Optional<Profile> profile = profileService.findByUser(user);
                    if (profile.isPresent()) {
                        session.setAttribute("theme", profile.get().getTheme());
                        session.setAttribute("sideNavigation", profile.get().getSideNavigation());
                        session.setAttribute("storeLayout", profile.get().getStoreLayout());
                    } else {
                        session.setAttribute("theme", "default");
                        session.setAttribute("sideNavigation", "expand");
                        session.setAttribute("storeLayout", "grid");
                    }
                    response.sendRedirect(request.getContextPath());
                }).failureHandler((request, response, exception) -> {
                    HttpSession session = request.getSession();
                    session.setAttribute("msgError", "Incorrect Username or Password!");
                    response.sendRedirect("/login");
                })
                .and().exceptionHandling().accessDeniedHandler((request, response, accessDeniedException) -> {
                    HttpSession session = request.getSession();
                    session.setAttribute("msgError", "Access Denied");
                    response.sendRedirect("/");
                })
                .and().build();
    }


}
