package net.dahliasolutions.services;

import lombok.RequiredArgsConstructor;
import net.dahliasolutions.data.DepartmentRepository;
import net.dahliasolutions.models.Department;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DepartmentService implements DepartmentServiceInterface {

    private final DepartmentRepository departmentRepository;

    @Override
    public Optional<Department> findById(BigInteger id) {
        return departmentRepository.findById(id);
    }

    @Override
    public Optional<Department> findByName(String departmentName) {
        return departmentRepository.findByName(departmentName);
    }

    @Override
    public List<Department> findAll() {
        return departmentRepository.findAll();
    }

    @Override
    public List<Department> findAllByManagerId(BigInteger managerId) {
        return departmentRepository.findAllByManagerId(managerId);
    }

    @Override
    public Department createDepartment(String name) {
        Department department = new Department(null, name, null, null, "blue");
        return departmentRepository.save(department);
    }

    @Override
    public void updateDepartment(Department department) {
        departmentRepository.save(department);
    }

    @Override
    public void deleteDepartmentById(BigInteger id) {
        departmentRepository.deleteById(id);
    }
}
