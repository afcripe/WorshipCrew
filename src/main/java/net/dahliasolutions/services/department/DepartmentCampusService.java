package net.dahliasolutions.services.department;

import lombok.RequiredArgsConstructor;
import net.dahliasolutions.data.CampusRepository;
import net.dahliasolutions.data.DepartmentCampusRepository;
import net.dahliasolutions.data.UserRepository;
import net.dahliasolutions.models.campus.Campus;
import net.dahliasolutions.models.department.DepartmentCampus;
import net.dahliasolutions.models.department.DepartmentRegional;
import net.dahliasolutions.models.user.User;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DepartmentCampusService implements DepartmentCampusServiceInterface {

    private final DepartmentCampusRepository departmentCampusRepository;
    private final UserRepository userRepository;
    private final CampusRepository campusRepository;

    @Override
    public Optional<DepartmentCampus> findById(BigInteger id) {
        return updateDepartmentDirectorName(departmentCampusRepository.findById(id));
    }

    @Override
    public Optional<DepartmentCampus> findByNameAndCampus(String departmentName, Campus campus) {
        return updateDepartmentDirectorName(departmentCampusRepository.findByNameAndCampus(departmentName, campus));
    }

    @Override
    public List<DepartmentCampus> findAll() {
        List<DepartmentCampus> departmentCampusList = departmentCampusRepository.findAll();
        for(DepartmentCampus dep : departmentCampusList) {
            updateDepartmentDirectorName(departmentCampusRepository.findById(dep.getId()));
        }
        return departmentCampusList;
    }

    @Override
    public List<DepartmentCampus> findAllByCampus(Campus campus) {
        return departmentCampusRepository.findAllByCampus(campus);
    }

    @Override
    public List<DepartmentCampus> findAllByDirectorId(BigInteger directorId) {
        List<DepartmentCampus> departmentCampusList = departmentCampusRepository.findAllByDirectorId(directorId);
        for(DepartmentCampus dep : departmentCampusList) {
            updateDepartmentDirectorName(departmentCampusRepository.findById(dep.getId()));
        }
        return departmentCampusList;
    }

    @Override
    public List<DepartmentCampus> findDepartmentCampusesByRegionalDepartment(DepartmentRegional department) {
        List<DepartmentCampus> departmentCampusList =
                departmentCampusRepository.findDepartmentCampusesByRegionalDepartment(department);
        for(DepartmentCampus dep : departmentCampusList) {
            updateDepartmentDirectorName(departmentCampusRepository.findById(dep.getId()));
        }
        return departmentCampusList;
    }

    @Override
    public List<DepartmentCampus> createByDepartmentRegional(DepartmentRegional departmentRegional) {
        List<Campus> campusList = campusRepository.findAll();
        List<DepartmentCampus> departmentCampusList = new ArrayList<>();
        for (Campus campus : campusList) {
            DepartmentCampus department = new DepartmentCampus();
            department.setName(departmentRegional.getName());
            department.setDirectorId(departmentRegional.getDirectorId());
            department.setDirectorName(departmentRegional.getDirectorName());
            department.setRegionalDepartment(departmentRegional);
            department.setCampus(campus);
            departmentCampusList.add(departmentCampusRepository.save(department));
        }
        return departmentCampusList;
    }


    @Override
    public void deleteByDepartmentRegional(DepartmentRegional departmentRegional) {
        List<DepartmentCampus> departmentList =
                departmentCampusRepository.findDepartmentCampusesByRegionalDepartment(departmentRegional);
        for (DepartmentCampus dep : departmentList) {
            departmentCampusRepository.deleteById(dep.getId());
        }
    }

    @Override
    public void save(DepartmentCampus departmentCampus) {
        departmentCampusRepository.save(updateDepartmentDirectorName(Optional.ofNullable(departmentCampus)).get());
    }

    @Override
    public Optional<DepartmentCampus> updateDepartmentDirectorName(Optional<DepartmentCampus> department) {
        if (department.isPresent() && department.get().getDirectorId() != null) {
            Optional<User> mgr = userRepository.findById(department.get().getDirectorId());
            if (mgr.isPresent()) {
                department.get().setDirectorName(mgr.get().getFirstName()+" "+mgr.get().getLastName());
            } else {
                department.get().setDirectorName("");
            }
            return department;
        }
        return department;
    }
}
