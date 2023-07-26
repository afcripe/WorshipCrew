package net.dahliasolutions.config;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import net.dahliasolutions.data.UserRepository;
import net.dahliasolutions.models.user.User;
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

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final UserRepository userRepository;

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
                    auth.requestMatchers("/accessdenied").permitAll();
                    auth.requestMatchers("/error").permitAll();
                    auth.requestMatchers("/forgotpassword").permitAll();
                    auth.requestMatchers("/passwordreset").permitAll();
                    auth.requestMatchers("/index").permitAll();
                    auth.requestMatchers("/login").permitAll();
                    auth.requestMatchers("/logout").permitAll();
                    auth.requestMatchers("/signin").permitAll();
                    auth.requestMatchers("/css/**").permitAll();
                    auth.requestMatchers("/img/**").permitAll();
                    auth.requestMatchers("/content/**").permitAll();
                    auth.requestMatchers("/fonts/**").permitAll();
                    auth.requestMatchers("/js/**").permitAll();
                    auth.requestMatchers("/mailer/**").permitAll();
                    auth.requestMatchers("/admin/**").hasAnyAuthority("ADMIN_READ", "ADMIN_WRITE", "DIRECTOR_READ", "DIRECTOR_WRITE", "CAMPUS_WRITE", "CAMPUS_READ", "USER_READ", "USER_WRITE");
                    auth.requestMatchers("/user/**").hasAnyAuthority("ADMIN_READ", "ADMIN_WRITE", "DIRECTOR_READ", "DIRECTOR_WRITE", "CAMPUS_WRITE", "CAMPUS_READ", "USER_READ", "USER_WRITE");
                    auth.requestMatchers("/campus/**").hasAnyAuthority("ADMIN_READ", "ADMIN_WRITE", "DIRECTOR_READ", "DIRECTOR_WRITE", "CAMPUS_WRITE", "CAMPUS_READ");
                    auth.requestMatchers("/position/**").hasAnyAuthority("ADMIN_READ", "ADMIN_WRITE", "DIRECTOR_READ", "DIRECTOR_WRITE");
                    auth.requestMatchers("/api/v1/app/removeerrormsg").permitAll();
                    auth.requestMatchers("/api/v1/app/toggleSideNav/*").permitAll();
                    auth.anyRequest().authenticated();
                })
                .formLogin().loginPage("/login").defaultSuccessUrl("/user/").successHandler((request, response, authentication) -> {
                    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
                    User user = (User) auth.getPrincipal();
                    HttpSession session = request.getSession();
                    session.setAttribute("userDisplayName", user.getFirstName());
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
