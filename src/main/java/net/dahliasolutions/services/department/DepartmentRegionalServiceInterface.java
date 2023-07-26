package net.dahliasolutions.services.department;

import net.dahliasolutions.models.department.DepartmentRegional;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;

public interface DepartmentRegionalServiceInterface {

    Optional<DepartmentRegional> findById(BigInteger id);
    Optional<DepartmentRegional> findByName(String departmentName);
    List<DepartmentRegional> findAll();
    List<DepartmentRegional> findAllByDirectorId(BigInteger directorId);
    DepartmentRegional createDepartment(String name);
    void updateDepartment(DepartmentRegional departmentRegional);
    void deleteDepartmentById(BigInteger id);
    Optional<DepartmentRegional> updateDepartmentDirectorName(Optional<DepartmentRegional> department);
}
