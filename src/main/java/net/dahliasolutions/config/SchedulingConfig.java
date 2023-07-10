package net.dahliasolutions.config;

import lombok.RequiredArgsConstructor;
import net.dahliasolutions.models.User;
import net.dahliasolutions.services.AdminSettingsService;
import net.dahliasolutions.services.EmailService;
import net.dahliasolutions.services.UserService;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.List;

@Configuration
@EnableScheduling
@RequiredArgsConstructor
public class SchedulingConfig {

    private final AdminSettingsService adminSettingsService;
    private final UserService userService;
    private final EmailService emailService;

    @Scheduled(cron="0 0 10 1 * ?", zone="America/Chicago")
    public void sendMonthlyStatements() {
        System.out.println("Run Task");
//        if (adminSettingsService.getAdminSettings().isMonthlyStatements()) {
//            List<User> userList = userService.findAllByActivated();
//            for(User user : userList) {
//                emailService.sendStatement(user);
//            }
//        }
    }

}
