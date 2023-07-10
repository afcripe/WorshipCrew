package net.dahliasolutions.services;

import net.dahliasolutions.models.Department;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;

public interface DepartmentServiceInterface {

    Optional<Department> findById(BigInteger id);
    Optional<Department> findByName(String departmentName);
    List<Department> findAll();
    List<Department> findAllByManagerId(BigInteger managerId);
    Department createDepartment(String name);
    void updateDepartment(Department position);
    void deleteDepartmentById(BigInteger id);
}
