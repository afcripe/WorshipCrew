package net.dahliasolutions;

import net.dahliasolutions.models.*;
import net.dahliasolutions.models.campus.Campus;
import net.dahliasolutions.models.department.DepartmentRegional;
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
import java.util.Optional;

@SpringBootApplication
public class Main {

    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
        AppServer appServer = getAppServerSingleton();
        appServer.setStaticFiles(false);
        System.out.println(appServer.getBaseURL());
    }

//    @Bean
//    @Scope("singleton")
//    static public AppServer getAppServerSingleton(){
//        return new AppServer("https://www.destinyworshipexchange.com",
//                "/var/destinyworshipexchange/content",
//                "/content");
//    }

    @Bean
    @Scope("singleton")
    static public AppServer getAppServerSingleton(){
        return new AppServer("http://localhost:8081",
                "/Users/afcripe/var/destinyworshipexchange/content",
                "/content");
    }

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
//            roleService.createRole("USER_SUPERVISOR", "Has full access to view, manage, and assign support tickets");
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
//
//            User u1 = new User();
//                u1.setUsername("Jake@destinyworship.com");
//                u1.setPassword("password");
//                u1.setFirstName("Jake");
//                u1.setLastName("Smith");
//                u1.setContactEmail("afcripe@live.com");
//                u1 = userService.createDefaultUser(u1);
//            u1.setDirector(u1);
//            userService.save(u1);
//            userService.addRoleToUser(u1.getUsername(), "DIRECTOR_WRITE");
//            userService.updateUserPosition(u1.getUsername(), "Regional Director");
//            userService.updateUserCampus(u1.getUsername(), "Destin");
//            userService.updateUserDepartment(u1.getUsername(), "Worship");
//
//            User u = new User();
//                u.setUsername("caleb@destinyworship.com");
//                u.setPassword("password");
//                u.setFirstName("Caleb");
//                u.setLastName("Lawrence");
//                u.setContactEmail("afcripe@live.com");
//                u = userService.createDefaultUser(u);
//            u.setDirector(u1);
//            u.setNotificationEndPoint(NotificationEndPoint.Push);
//            userService.save(u);
//            userService.addRoleToUser(u.getUsername(), "ADMIN_WRITE");
//            userService.updateUserPosition(u.getUsername(), "Regional Director");
//            userService.updateUserCampus(u.getUsername(), "Destin");
//            userService.updateUserDepartment(u.getUsername(), "Production");
//
//            User u2 = new User();
//                u2.setUsername("aiden@destinyworship.com");
//                u2.setPassword("password");
//                u2.setFirstName("Aiden");
//                u2.setLastName("Vaughn");
//                u2.setContactEmail("afcripe@live.com");
//                u2 = userService.createDefaultUser(u2);
//                u2.setDirector(u);
//            userService.save(u2);
//            userService.addRoleToUser(u2.getUsername(), "CAMPUS_WRITE");
//            userService.updateUserPosition(u2.getUsername(), "Director");
//            userService.updateUserCampus(u2.getUsername(), "Destin");
//            userService.updateUserDepartment(u2.getUsername(), "Production");
//
//            User u4 = new User();
//                u4.setUsername("steven@destinyworship.com");
//                u4.setPassword("password");
//                u4.setFirstName("Steven");
//                u4.setLastName("Theriot");
//                u4.setContactEmail("afcripe@live.com");
//                u4 = userService.createDefaultUser(u4);
//                u4.setDirector(u1);
//            userService.save(u4);
//            userService.addRoleToUser(u4.getUsername(), "CAMPUS_WRITE");
//            userService.updateUserPosition(u4.getUsername(), "Director");
//            userService.updateUserCampus(u4.getUsername(), "Destin");
//            userService.updateUserDepartment(u4.getUsername(), "Worship");
//
//            User u5 = new User();
//                u5.setUsername("adam@destinyworship.com");
//                u5.setPassword("password");
//                u5.setFirstName("Adam");
//                u5.setLastName("Mills");
//                u5.setContactEmail("afcripe@live.com");
//                u5 = userService.createDefaultUser(u5);
//                u5.setDirector(u1);
//            userService.save(u5);
//            userService.addRoleToUser(u5.getUsername(), "CAMPUS_WRITE");
//            userService.updateUserPosition(u5.getUsername(), "Director");
//            userService.updateUserCampus(u5.getUsername(), "FWB");
//            userService.updateUserDepartment(u5.getUsername(), "Production");
//
//            User u6 = new User();
//                u6.setUsername("afcripe@live.com");
//                u6.setPassword("password");
//                u6.setFirstName("Andrew");
//                u6.setLastName("Cripe");
//                u6.setContactEmail("afcripe@live.com");
//                u6 = userService.createDefaultUser(u6);
//                u6.setDirector(u);
//            userService.save(u6);
//            userService.addRoleToUser(u6.getUsername(), "ADMIN_WRITE");
//            userService.updateUserPosition(u6.getUsername(), "Leader");
//            userService.updateUserCampus(u6.getUsername(), "Destin");
//            userService.updateUserDepartment(u6.getUsername(), "Production");
//
//
//            mainLocation.setDirectorId(u1.getId());
//            campusService.save(mainLocation);
//            location1.setDirectorId(u5.getId());
//            campusService.save(location1);
//            location2.setDirectorId(u1.getId());
//            campusService.save(location2);
//            location3.setDirectorId(u1.getId());
//            campusService.save(location3);
//            location4.setDirectorId(u1.getId());
//            campusService.save(location4);
//
//            Optional<DepartmentRegional> worship = departmentRegionalService.findByName("Worship");
//            if(worship.isPresent()) {
//                worship.get().setDirectorId(u1.getId());
//                departmentRegionalService.save(worship.get());
//            }
//
//            Optional<DepartmentRegional> production = departmentRegionalService.findByName("Production");
//            if(production.isPresent()) {
//                production.get().setDirectorId(u.getId());
//                departmentRegionalService.save(production.get());
//            }
//
//        };
//
//    }

}
