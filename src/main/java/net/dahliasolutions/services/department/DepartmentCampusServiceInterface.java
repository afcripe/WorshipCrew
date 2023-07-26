package net.dahliasolutions.services.department;

import net.dahliasolutions.models.campus.Campus;
import net.dahliasolutions.models.department.DepartmentCampus;
import net.dahliasolutions.models.department.DepartmentRegional;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;

public interface DepartmentCampusServiceInterface {

    Optional<DepartmentCampus> findById(BigInteger id);
    Optional<DepartmentCampus> findByNameAndCampus(String departmentName, Campus campus);
    List<DepartmentCampus> findAll();
    List<DepartmentCampus> findAllByCampus(Campus campus);
    List<DepartmentCampus> findAllByDirectorId(BigInteger directorId);
    List<DepartmentCampus> findDepartmentCampusesByRegionalDepartment(DepartmentRegional department);
    List<DepartmentCampus> createByDepartmentRegional(DepartmentRegional departmentRegional);
    void deleteByDepartmentRegional(DepartmentRegional departmentRegional);
    void updateDepartment(DepartmentCampus departmentCampus);
    Optional<DepartmentCampus> updateDepartmentDirectorName(Optional<DepartmentCampus> department);
}
