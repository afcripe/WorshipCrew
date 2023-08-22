package net.dahliasolutions.controllers.user;

import lombok.RequiredArgsConstructor;
import net.dahliasolutions.models.UniversalSearchModel;
import net.dahliasolutions.models.department.DepartmentCampus;
import net.dahliasolutions.models.department.DepartmentRegional;
import net.dahliasolutions.models.order.OrderRequest;
import net.dahliasolutions.models.records.CampusDepartmentModel;
import net.dahliasolutions.models.records.SingleStringModel;
import net.dahliasolutions.models.campus.Campus;
import net.dahliasolutions.models.user.User;
import net.dahliasolutions.models.user.UserRoles;
import net.dahliasolutions.services.campus.CampusService;
import net.dahliasolutions.services.department.DepartmentCampusService;
import net.dahliasolutions.services.department.DepartmentRegionalService;
import net.dahliasolutions.services.user.UserRolesService;
import net.dahliasolutions.services.user.UserService;
import org.springframework.web.bind.annotation.*;

import javax.swing.text.html.Option;
import java.math.BigInteger;
import java.util.*;

@RestController
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
public class UserAPIController {

    private final UserService userService;
    private final UserRolesService userRolesService;
    private final CampusService campusService;
    private final DepartmentRegionalService departmentRegionalService;
    private final DepartmentCampusService departmentCampusService;

    @GetMapping("")
    public List<User> goUserHome() {
        return userService.findAll();
    }

    @GetMapping ("/campus/{campus}")
    public List<User> getUserList(@PathVariable String campus) {
        Optional<Campus> campusOption = campusService.findByName(campus);
        return userService.findAllByCampus(campusOption.get());
    }

    @PostMapping("/setrole/{id}")
    public String setUserRole(@ModelAttribute SingleStringModel role, @PathVariable BigInteger id) {
        Optional<UserRoles> userRole = userRolesService.findByName(role.name());
        Optional<User> user = userService.findById(id);
        boolean found = false;
        if (userRole.isPresent() && user.isPresent()) {
            Collection<UserRoles> newRoles = new ArrayList<>();
            for (UserRoles r : user.get().getUserRoles()) {
                if (!r.getName().equals(userRole.get().getName())) {
                    newRoles.add(r);
                } else {
                    found = true;
                }
            }
            if (!found) {
                newRoles.add(userRole.get());
            }

            user.get().setUserRoles(newRoles);
            userService.save(user.get());
            return "true";
        }
        return "false";
    }

    @PostMapping("/filteredusers")
    public List<User> filteredUserList(@ModelAttribute CampusDepartmentModel filterModel) {
        // init return
        List<User> userListReturn;
//        Integer idString = Integer.parseInt(filterModel.userId());
//        BigInteger id = BigInteger.valueOf(idString);
        Optional<Campus> campus = campusService.findByName(filterModel.campus());
        Optional<User> user = userService.findById(filterModel.userId());
        Optional<DepartmentRegional> regionalDep = departmentRegionalService.findByName(filterModel.department());

        Optional<User> director = Optional.empty();
        if (user.isPresent()) { director = userService.findById(regionalDep.get().getDirectorId()); }


        if (campus.isEmpty()) {
            if (regionalDep.isPresent()) {
                userListReturn = userService.findAllByDepartment(regionalDep.get());
                if (director.isPresent()) {
                    if (!userListReturn.contains(director.get())) {
                        userListReturn.add(director.get());
                    }
                }
            } else {
                userListReturn = userService.findAll();
            }

        } else {
            Optional<DepartmentCampus> campusDep = departmentCampusService.findByNameAndCampus(filterModel.department(), campus.get());
            if (campusDep.isPresent()) {
                userListReturn = userService.findAllByDepartmentCampus(campusDep.get());
                if (director.isPresent()) {
                    if (!userListReturn.contains(director.get())) {
                        userListReturn.add(director.get());
                    }
                }
            } else {
                userListReturn = userService.findAll();
            }
        }

        if (user.isPresent()) { userListReturn.add(user.get()); }

        Collections.sort(userListReturn, new Comparator<User>() {
            @Override
            public int compare(User user1, User user2) {
                return user1.getFullName().compareToIgnoreCase(user2.getFullName());
            }
        });

        return userListReturn;
    }

    @PostMapping("/search")
    public List<UniversalSearchModel> searchRequests(@ModelAttribute SingleStringModel stringModel) {
        // init return
        List<UniversalSearchModel> searchReturn = new ArrayList<>();

        List<User> foundUsers = userService.searchAllByFullName(stringModel.name());
        for (User user : foundUsers) {
            searchReturn.add(new UniversalSearchModel(user.getFullName(), "user", user.getId()));
        }

        return searchReturn;
    }
}
