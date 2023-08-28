package net.dahliasolutions.services.department;

import lombok.RequiredArgsConstructor;
import net.dahliasolutions.data.DepartmentCampusRepository;
import net.dahliasolutions.data.DepartmentRegionalRepository;
import net.dahliasolutions.data.UserRepository;
import net.dahliasolutions.models.department.DepartmentCampus;
import net.dahliasolutions.models.department.DepartmentRegional;
import net.dahliasolutions.models.user.User;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DepartmentRegionalService implements DepartmentRegionalServiceInterface {

    private final DepartmentRegionalRepository departmentRegionalRepository;
    private final DepartmentCampusRepository departmentCampusRepository;
    private final UserRepository userRepository;
    private final DepartmentCampusService departmentCampusService;

    @Override
    public Optional<DepartmentRegional> findById(BigInteger id) {
        return updateDepartmentDirectorName(departmentRegionalRepository.findById(id));
    }

    @Override
    public Optional<DepartmentRegional> findByName(String departmentName) {
        return updateDepartmentDirectorName(departmentRegionalRepository.findByName(departmentName));
    }

    @Override
    public List<DepartmentRegional> findAll() {
        List<DepartmentRegional> departmentRegionalList = departmentRegionalRepository.findAll();
        for(DepartmentRegional dep : departmentRegionalList) {
            updateDepartmentDirectorName(departmentRegionalRepository.findById(dep.getId()));
        }
        return departmentRegionalList;
    }

    @Override
    public List<DepartmentRegional> findAllByDirectorId(BigInteger directorId) {
        List<DepartmentRegional> departmentRegionalList = departmentRegionalRepository.findAllByDirectorId(directorId);
        for(DepartmentRegional dep : departmentRegionalList) {
            updateDepartmentDirectorName(departmentRegionalRepository.findById(dep.getId()));
        }
        return departmentRegionalList;
    }

    @Override
    public DepartmentRegional createDepartment(String name) {
        DepartmentRegional departmentRegional = new DepartmentRegional(null, name, null, "");
        departmentRegionalRepository.save(departmentRegional);
        List<DepartmentCampus> depCamp = departmentCampusService.createByDepartmentRegional(departmentRegional);
        return departmentRegional;
    }

    @Override
    public void save(DepartmentRegional departmentRegional) {
        departmentRegionalRepository.save(updateDepartmentDirectorName(Optional.ofNullable(departmentRegional)).get());
    }

    @Override
    public void deleteDepartmentById(BigInteger id) {
        Optional<DepartmentRegional> regional = departmentRegionalRepository.findById(id);
        if (regional.isPresent()) {
            List<DepartmentCampus> campusList = departmentCampusRepository.findDepartmentCampusesByRegionalDepartment(regional.get());
            for (DepartmentCampus departmentCampus : campusList) {
                departmentCampusRepository.delete(departmentCampus);
            }
        }
        departmentRegionalRepository.deleteById(id);
    }

    @Override
    public Optional<DepartmentRegional> updateDepartmentDirectorName(Optional<DepartmentRegional> department) {
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
