package net.dahliasolutions.config;

import lombok.RequiredArgsConstructor;
import net.dahliasolutions.services.AdminSettingsService;
import net.dahliasolutions.services.mail.EmailService;
import net.dahliasolutions.services.mail.NotificationMessageService;
import net.dahliasolutions.services.user.UserService;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@Configuration
@EnableScheduling
@RequiredArgsConstructor
public class SchedulingConfig {

    private final AdminSettingsService adminSettingsService;
    private final UserService userService;
    private final EmailService emailService;
    private final NotificationMessageService messageService;

    @Scheduled(cron="0 0 10 1 * ?", zone="America/Chicago")
    public void runMonthly() {
        System.out.println("Run Task Every Month");
//        if (adminSettingsService.getAdminSettings().isMonthlyStatements()) {
//            List<User> userList = userService.findAllByActivated();
//            for(User user : userList) {
//                emailService.sendStatement(user);
//            }
//        }
    }

    @Scheduled(cron="0 0/2 * * * ?", zone="America/Chicago")
    public void runEvery2Minutes() {
        messageService.sendAllMessages();
//        System.out.println("Run Task Every 2 Minutes");
//        if (adminSettingsService.getAdminSettings().isMonthlyStatements()) {
//            List<User> userList = userService.findAllByActivated();
//            for(User user : userList) {
//                emailService.sendStatement(user);
//            }
//        }
    }

}
