package net.dahliasolutions;

import jakarta.servlet.http.HttpSession;
import net.dahliasolutions.models.*;
import net.dahliasolutions.models.campus.Campus;
import net.dahliasolutions.models.position.Position;
import net.dahliasolutions.models.user.User;
import net.dahliasolutions.models.wiki.WikiTag;
import net.dahliasolutions.services.NotificationService;
import net.dahliasolutions.services.campus.CampusService;
import net.dahliasolutions.services.department.DepartmentRegionalService;
import net.dahliasolutions.services.position.PositionService;
import net.dahliasolutions.services.user.UserRolesService;
import net.dahliasolutions.services.user.UserService;
import net.dahliasolutions.services.wiki.WikiFolderService;
import net.dahliasolutions.services.wiki.WikiTagService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Optional;

//import java.math.BigInteger;

@SpringBootApplication
public class Main {
    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
        System.out.println(getAppServerSingleton().getBaseURL());
    }

    @Bean
    @Scope("singleton")
    static public AppServer getAppServerSingleton(){
        return new AppServer("https://www.destinyworshipexchange.com",
                "/var/destinyworshipexchange/content",
                "/content");
    }

//    @Bean
//    @Scope("singleton")
//    static public AppServer getAppServerSingleton(){
//        return new AppServer("http://localhost:8081",
//                "/Users/afcripe/var/destinyworshipexchange/content",
//                "/content");
//    }

//    @Bean
//    CommandLineRunner run(UserRolesService roleService, PositionService positionService, WikiTagService wikiTagService,
//                          UserService userService, CampusService campusService, DepartmentRegionalService departmentRegionalService,
//                          WikiFolderService wikiFolderService, NotificationService notificationService) {
//        return args -> {
//
//            wikiTagService.createWikiTag(new WikiTag(null, "production"));
//            wikiTagService.createWikiTag(new WikiTag(null, "worship"));
//            wikiTagService.createWikiTag(new WikiTag(null, "audio"));
//            wikiFolderService.save("/general");
//
//            roleService.createRole("RESOURCE_READ", "View resources");
//            roleService.createRole("RESOURCE_WRITE", "Create new resources");
//            roleService.createRole("RESOURCE_SUPERVISOR", "Has full access to resources and resource settings");
//            roleService.createRole("STORE_READ", "Browse that store and request items within assigned department");
//            roleService.createRole("STORE_WRITE", "Create new items in the store for assigned department");
//            roleService.createRole("STORE_SUPERVISOR", "Has full access to store and store settings");
//            roleService.createRole("REQUEST_WRITE", "Update, assign, and fulfill user requests");
//            roleService.createRole("REQUEST_SUPERVISOR", "Full access to user requests");
//            roleService.createRole("SUPPORT_READ", "View support tickets");
//            roleService.createRole("SUPPORT_WRITE", "Create support tickets");
//            roleService.createRole("SUPPORT_AGENT", "Can be assigned support tickets");
//            roleService.createRole("SUPPORT_SUPERVISOR", "Has full access to view, manage, and assign support tickets");
//            roleService.createRole("USER_READ", "View user information for assigned campus");
//            roleService.createRole("USER_WRITE", "Create and edite users for assigned campus");
//            roleService.createRole("CAMPUS_READ", "View all information for assigned campus");
//            roleService.createRole("CAMPUS_WRITE", "Edite information for assigned campus");
//            roleService.createRole("DIRECTOR_READ", "View all information regionally for assigned department");
//            roleService.createRole("DIRECTOR_WRITE", "Edit all information regionally for assigned department, and manage positions");
//            roleService.createRole("ADMIN_WRITE", "Has full access");
//
//            Campus mainLocation = campusService.createCampus("Destin", "Destin", BigInteger.valueOf(0));
//            Campus location1 = campusService.createCampus("FWB", "Fort Walton Beach", BigInteger.valueOf(1));
//            Campus location2 = campusService.createCampus("Freeport", "Freeport", BigInteger.valueOf(2));
//            Campus location3 = campusService.createCampus("Crestview", "Crestview", BigInteger.valueOf(3));
//            Campus location4 = campusService.createCampus("PCB", "Panama City Beach", BigInteger.valueOf(4));
//            Campus location5 = campusService.createCampus("Navarre", "Navarre", BigInteger.valueOf(5));
//
//            Position regionalDirector = positionService.createPosition("Regional Director");
//                regionalDirector.setLevel(1);
//                positionService.save(regionalDirector);
//            Position director = positionService.createPosition("Director");
//                director.setLevel(2);
//                positionService.save(director);
//            positionService.save(director);
//            positionService.createPosition("Leader");
//            positionService.createPosition("Volunteer");
//
//            departmentRegionalService.createDepartment("Worship");
//            departmentRegionalService.createDepartment("Production");
//
//            User u = new User();
//                u.setUsername("caleb@destinyworship.com");
//                u.setPassword("password");
//                u.setFirstName("Caleb");
//                u.setLastName("Lawrence");
//                u.setContactEmail("afcripe@live.com");
//                u = userService.createDefaultUser(u);
//            u.setDirector(u);
//            userService.save(u);
//            userService.addRoleToUser(u.getUsername(), "ADMIN_WRITE");
//            userService.updateUserPosition(u.getUsername(), "Regional Director");
//            userService.updateUserCampus(u.getUsername(), "Destin");
//            userService.updateUserDepartment(u.getUsername(), "Production");
//
//            User u2 = new User();
//                u2.setUsername("afcripe@live.com");
//                u2.setPassword("password");
//                u2.setFirstName("Andrew");
//                u2.setLastName("Cripe");
//                u2.setContactEmail("afcripe@live.com");
//                u2.setDirector(u);
//                u2 = userService.createDefaultUser(u2);
//            userService.addRoleToUser(u2.getUsername(), "ADMIN_WRITE");
//            userService.updateUserPosition(u2.getUsername(), "Leader");
//            userService.updateUserCampus(u2.getUsername(), "Destin");
//            userService.updateUserDepartment(u2.getUsername(), "Production");
//
//            User u3 = new User();
//                u3.setUsername("aidan@destinyworship.com");
//                u3.setPassword("password");
//                u3.setFirstName("Aidan");
//                u3.setLastName("Vaughn");
//                u3.setContactEmail("afcripe@live.com");
//                u3.setDirector(u);
//                u3 = userService.createDefaultUser(u3);
//            userService.addRoleToUser(u3.getUsername(), "ADMIN_WRITE");
//            userService.updateUserPosition(u3.getUsername(), "Leader");
//            userService.updateUserCampus(u3.getUsername(), "Destin");
//            userService.updateUserDepartment(u3.getUsername(), "Production");
//
//            mainLocation.setDirectorId(u.getId());
//            mainLocation.setDirectorName("Caleb Lawrence");
//            campusService.save(mainLocation);
//        };
//
//    }

}
