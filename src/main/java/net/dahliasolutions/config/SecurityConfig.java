package net.dahliasolutions.config;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import net.dahliasolutions.data.*;
import net.dahliasolutions.models.user.Profile;
import net.dahliasolutions.models.user.User;
import net.dahliasolutions.models.user.UserRoles;
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

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final UserRepository userRepository;
    private final ProfileService profileService;
    private final UserRolesRepository rolesRepository;
    private final PositionRepository positionRepository;
    private final CampusRepository campusRepository;
    private final DepartmentCampusRepository departmentCampusRepository;

    @Bean
    public UserDetailsService userDetailsService() {
        return username -> userInterceptor(username)
                .orElseThrow(() -> new UsernameNotFoundException("Username Not Found."));
    }
    private Optional<User> userInterceptor(String username) {
        if(username.equals("admin@admin.com")) {
            List<UserRoles> roles = new ArrayList<>();
            roles.add(rolesRepository.findByName("ADMIN_WRITE").get());
            return Optional.ofNullable(User.builder()
                    .id(BigInteger.valueOf(0))
                    .username("admin@admin.com")
                    .contactEmail("andrew@dahliasolutions.net")
                    .firstName("Admin").lastName("Admin")
                    .password("$2a$10$c0KujdAN776fInjdk08fd.kFnqBL5spBnm0yOaA7GJnmpUTT5dGc2")
                    .activated(true).userRoles(roles)
                    .position(positionRepository.findById(BigInteger.valueOf(1)).get())
                    .campus(campusRepository.findById(BigInteger.valueOf(1)).get())
                    .department(departmentCampusRepository.findById(BigInteger.valueOf(1)).get())
                    .deleted(false).subscriptions(new ArrayList<>()).endpoints(new ArrayList<>())
                    .build());
        }
        return userRepository.findByUsernameIgnoreCase(username);
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
                    auth.requestMatchers("/firebase-messaging-sw.js").permitAll();
                    auth.requestMatchers("/login").permitAll();
                    auth.requestMatchers("/forgotpassword").permitAll();
                    auth.requestMatchers("/signin").permitAll();
                    auth.requestMatchers("/login").permitAll();
                    auth.requestMatchers("/logout").permitAll();
                    auth.requestMatchers("/passwordreset").permitAll();
                    auth.requestMatchers("/mailer").permitAll();
                    auth.requestMatchers("/mailer/*").permitAll();
                    auth.requestMatchers("/app/mailer/**").permitAll();

                    auth.requestMatchers("/css/**").permitAll();
                    auth.requestMatchers("/img/**").permitAll();
                    auth.requestMatchers("/content/**").permitAll();
                    auth.requestMatchers("/fonts/**").permitAll();
                    auth.requestMatchers("/js/**").permitAll();

                    auth.requestMatchers("/app").permitAll();
                    auth.requestMatchers("/manifest.json").permitAll();
                    auth.requestMatchers("/app/**").permitAll();
                    auth.requestMatchers("/spa/**").permitAll();

                    auth.requestMatchers("/store").hasAnyAuthority("ADMIN_WRITE", "STORE_SUPERVISOR", "STORE_READ","STORE_WRITE");
                    auth.requestMatchers("/store/item/**").hasAnyAuthority("ADMIN_WRITE", "STORE_SUPERVISOR", "STORE_READ","STORE_WRITE");
                    auth.requestMatchers("/store/edit/**").hasAnyAuthority("ADMIN_WRITE", "STORE_SUPERVISOR", "STORE_WRITE");
                    auth.requestMatchers("/store/settings/**").hasAnyAuthority("ADMIN_WRITE", "STORE_SUPERVISOR");

                    auth.requestMatchers("/request").hasAnyAuthority("ADMIN_WRITE", "REQUEST_SUPERVISOR", "REQUEST_WRITE");
                    auth.requestMatchers("/request/order/**").hasAnyAuthority("ADMIN_WRITE", "REQUEST_SUPERVISOR", "REQUEST_WRITE");
                    auth.requestMatchers("/request/department/**").hasAnyAuthority("ADMIN_WRITE", "REQUEST_SUPERVISOR");
                    auth.requestMatchers("/request/campus/**").hasAnyAuthority("ADMIN_WRITE", "REQUEST_SUPERVISOR");
                    auth.requestMatchers("/request/user/**").hasAnyAuthority("ADMIN_WRITE", "REQUEST_SUPERVISOR");
                    auth.requestMatchers("/request/settings/**").hasAnyAuthority("ADMIN_WRITE", "REQUEST_SUPERVISOR");

                    auth.requestMatchers("/resource/article/**").permitAll();
                    auth.requestMatchers("/resource").hasAnyAuthority("ADMIN_WRITE", "RESOURCE_SUPERVISOR", "RESOURCE_WRITE", "RESOURCE_READ");
                    auth.requestMatchers("/resource/tag/**").hasAnyAuthority("ADMIN_WRITE", "RESOURCE_SUPERVISOR", "RESOURCE_WRITE", "RESOURCE_READ");
                    auth.requestMatchers("/resource/folder/**").hasAnyAuthority("ADMIN_WRITE", "RESOURCE_SUPERVISOR", "RESOURCE_WRITE", "RESOURCE_READ");
                    auth.requestMatchers("/resource/group/**").hasAnyAuthority("ADMIN_WRITE", "RESOURCE_SUPERVISOR", "RESOURCE_WRITE", "RESOURCE_READ");
                    auth.requestMatchers("/resource/search/**").hasAnyAuthority("ADMIN_WRITE", "RESOURCE_SUPERVISOR", "RESOURCE_WRITE", "RESOURCE_READ");
                    auth.requestMatchers("/resource/new/**").hasAnyAuthority("ADMIN_WRITE", "RESOURCE_SUPERVISOR", "RESOURCE_WRITE");
                    auth.requestMatchers("/resource/edit/**").hasAnyAuthority("ADMIN_WRITE", "RESOURCE_SUPERVISOR", "RESOURCE_WRITE");
                    auth.requestMatchers("/resource/settings").hasAnyAuthority("ADMIN_WRITE", "RESOURCE_SUPERVISOR");
                    auth.requestMatchers("/resource/tagmanager").hasAnyAuthority("ADMIN_WRITE", "RESOURCE_SUPERVISOR");
                    auth.requestMatchers("/resource/foldermanager").hasAnyAuthority("ADMIN_WRITE", "RESOURCE_SUPERVISOR");

                    auth.requestMatchers("/support").hasAnyAuthority("ADMIN_WRITE", "SUPPORT_SUPERVISOR", "SUPPORT_AGENT", "SUPPORT_WRITE", "SUPPORT_READ");
                    auth.requestMatchers("/support/item/**").hasAnyAuthority("ADMIN_WRITE", "SUPPORT_SUPERVISOR", "SUPPORT_AGENT", "SUPPORT_WRITE", "SUPPORT_READ");
                    auth.requestMatchers("/support/new/**").hasAnyAuthority("ADMIN_WRITE", "SUPPORT_SUPERVISOR", "SUPPORT_AGENT", "SUPPORT_WRITE");
                    auth.requestMatchers("/request/settings/**").hasAnyAuthority("ADMIN_WRITE", "SUPPORT_SUPERVISOR");

                    auth.requestMatchers("/campus").hasAnyAuthority("ADMIN_WRITE", "USER_SUPERVISOR", "DIRECTOR_READ", "DIRECTOR_WRITE", "CAMPUS_WRITE", "CAMPUS_READ");
                    auth.requestMatchers("/campus/*").hasAnyAuthority("ADMIN_WRITE", "USER_SUPERVISOR", "DIRECTOR_READ", "DIRECTOR_WRITE", "CAMPUS_WRITE", "CAMPUS_READ");
                    auth.requestMatchers("/campus/campus/**").hasAnyAuthority("ADMIN_WRITE", "USER_SUPERVISOR", "DIRECTOR_READ", "DIRECTOR_WRITE", "CAMPUS_WRITE", "CAMPUS_READ");
                    auth.requestMatchers("/campus/showhidden").hasAnyAuthority("ADMIN_WRITE", "USER_SUPERVISOR");
                    auth.requestMatchers("/campus/new/**").hasAnyAuthority("ADMIN_WRITE", "USER_SUPERVISOR");
                    auth.requestMatchers("/campus/create/**").hasAnyAuthority("ADMIN_WRITE", "USER_SUPERVISOR");
                    auth.requestMatchers("/campus/edit/**").hasAnyAuthority("ADMIN_WRITE", "USER_SUPERVISOR");
                    auth.requestMatchers("/campus/update/**").hasAnyAuthority("ADMIN_WRITE", "USER_SUPERVISOR");

                    auth.requestMatchers("/department").hasAnyAuthority("ADMIN_WRITE", "USER_SUPERVISOR", "DIRECTOR_READ", "DIRECTOR_WRITE");
                    auth.requestMatchers("/department/*").hasAnyAuthority("ADMIN_WRITE", "USER_SUPERVISOR", "DIRECTOR_READ", "DIRECTOR_WRITE");
                    auth.requestMatchers("/department/edit/**").hasAnyAuthority("ADMIN_WRITE", "USER_SUPERVISOR");
                    auth.requestMatchers("/department/new/**").hasAnyAuthority("ADMIN_WRITE", "USER_SUPERVISOR");
                    auth.requestMatchers("/department/create/**").hasAnyAuthority("ADMIN_WRITE", "USER_SUPERVISOR");
                    auth.requestMatchers("/department/update/**").hasAnyAuthority("ADMIN_WRITE", "USER_SUPERVISOR");
                    auth.requestMatchers("/department/delete/**").hasAnyAuthority("ADMIN_WRITE", "USER_SUPERVISOR");

                    auth.requestMatchers("/position").hasAnyAuthority("ADMIN_WRITE", "USER_SUPERVISOR");
                    auth.requestMatchers("/position/**").hasAnyAuthority("ADMIN_WRITE", "USER_SUPERVISOR");
                    auth.requestMatchers("/permissiontemplate").hasAnyAuthority("ADMIN_WRITE", "USER_SUPERVISOR");
                    auth.requestMatchers("/permissiontemplate/**").hasAnyAuthority("ADMIN_WRITE", "USER_SUPERVISOR");

                    auth.requestMatchers("/user").hasAnyAuthority("ADMIN_WRITE", "USER_SUPERVISOR", "DIRECTOR_READ", "DIRECTOR_WRITE", "CAMPUS_WRITE", "CAMPUS_READ", "USER_READ", "USER_WRITE");
                    auth.requestMatchers("/useradmin").hasAnyAuthority("ADMIN_WRITE", "USER_SUPERVISOR");
                    auth.requestMatchers("/user/**").hasAnyAuthority("ADMIN_WRITE", "USER_SUPERVISOR", "DIRECTOR_READ", "DIRECTOR_WRITE", "CAMPUS_WRITE", "CAMPUS_READ", "USER_READ", "USER_WRITE");

                    auth.requestMatchers("/admin/**").hasAnyAuthority("ADMIN_WRITE", "DIRECTOR_READ", "DIRECTOR_WRITE", "CAMPUS_WRITE", "CAMPUS_READ", "USER_SUPERVISOR", "USER_READ", "USER_WRITE");

                    auth.requestMatchers("/api/v1/admin").permitAll();
                    auth.requestMatchers("/api/v1/auth/**").permitAll();
                    auth.requestMatchers("/api/v1/app/**").permitAll();

                    auth.requestMatchers("/api/v1/**").authenticated();

                    auth.anyRequest().authenticated();
                })
                .formLogin().loginPage("/login").successHandler((request, response, authentication) -> {
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
                    String savedRequest = "/";
                    if (session.getAttribute("SPRING_SECURITY_SAVED_REQUEST") != null) {
                        savedRequest = session.getAttribute("SPRING_SECURITY_SAVED_REQUEST").toString();
                        savedRequest = savedRequest.substring(21, savedRequest.length() - 1);
                        savedRequest = savedRequest.replace("?continue","");
                        savedRequest = savedRequest.replace("&continue","");
                    }
                    if (savedRequest.contains("api")) {
                        savedRequest = request.getContextPath();
                    }
                    session.setAttribute("loginRedirect", savedRequest);
                    response.sendRedirect(savedRequest);
                })
                .and().exceptionHandling().accessDeniedHandler((request, response, accessDeniedException) -> {
                    HttpSession session = request.getSession();
                    session.setAttribute("msgError", "Access Denied");
                    response.sendRedirect("/");
                })
                .and().build();
    }


}
