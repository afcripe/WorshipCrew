package net.dahliasolutions;

import net.dahliasolutions.models.*;
import net.dahliasolutions.services.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigInteger;

//import java.math.BigInteger;

@SpringBootApplication
@RestController
public class Main {
    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
        System.out.println(getAppServerSingleton().getBaseURL());
    }

    @Bean
    @Scope("singleton")
    static public AppServer getAppServerSingleton(){
        return new AppServer("https://www.legacyvolunteers.net");
    }

    @Bean
    CommandLineRunner run(UserRolesService roleService, PositionService positionService,
                          UserService userService, CampusService campusService, DepartmentService departmentService) {
        return args -> {
            roleService.createRole("USER_READ", "General user profile that can browse the exchange and place orders");
            roleService.createRole("USER_WRITE", "Has the ability to leave comments and update the forum");
            roleService.createRole("MANAGER_READ", "View user information for designated campus");
            roleService.createRole("MANAGER_WRITE", "Ability to edit designated campus information");
            roleService.createRole("DIRECTOR_READ", "View all campus information");
            roleService.createRole("DIRECTOR_WRITE", "Has full access to make changes to campuses and the exchange");
            roleService.createRole("ADMIN_READ", "Can view all site information");
            roleService.createRole("ADMIN_WRITE", "Has full access");

            positionService.createPosition("Worship Team");
            positionService.createPosition("Worship Leader");
            positionService.createPosition("Production Crew");
            positionService.createPosition("Production Lead");
            positionService.createPosition("Campus Lead");
            positionService.createPosition("Regional Director");

            departmentService.createDepartment("Worship");
            departmentService.createDepartment("Production");

            Campus mainLocation = campusService.createCampus("Destin", "Destin", BigInteger.valueOf(0));
            Campus location1 = campusService.createCampus("FWB", "Fort Walton Beach", BigInteger.valueOf(1));
            Campus location2 = campusService.createCampus("Freeport", "Freeport", BigInteger.valueOf(2));
            Campus location3 = campusService.createCampus("Crestview", "Crestview", BigInteger.valueOf(3));
            Campus location4 = campusService.createCampus("PCB", "Panama City Beach", BigInteger.valueOf(4));
            Campus location5 = campusService.createCampus("Navarre", "Navarre", BigInteger.valueOf(5));

            User u = new User();
                u.setUsername("afcripe@live.com");
                u.setPassword("password");
                u.setFirstName("Andrew");
                u.setLastName("Cripe");
                u.setContactEmail("afcripe@live.com");
                u = userService.createDefaultUser(u);
            userService.addRoleToUser(u.getUsername(), "ADMIN_WRITE");
            userService.updateUserPosition(u.getUsername(), "Administrator");
            userService.updateUserLocation(u.getUsername(), "Destin");

            mainLocation.setManagerId(u.getId());
            mainLocation.setManagerName("Andrew Cripe");
            campusService.save(mainLocation);
        };

    }
}
