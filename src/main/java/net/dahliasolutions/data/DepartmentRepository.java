package net.dahliasolutions.data;

import net.dahliasolutions.models.Department;
import net.dahliasolutions.models.Position;
import org.springframework.data.jpa.repository.JpaRepository;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;

public interface DepartmentRepository extends JpaRepository<Department, BigInteger> {

    Optional<Department> findByName(String departmentName);
    List<Department> findAllByManagerId(BigInteger managerId);
}
