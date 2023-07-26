package net.dahliasolutions.data;

import net.dahliasolutions.models.department.DepartmentRegional;
import org.springframework.data.jpa.repository.JpaRepository;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;

public interface DepartmentRegionalRepository extends JpaRepository<DepartmentRegional, BigInteger> {

    Optional<DepartmentRegional> findByName(String departmentName);
    List<DepartmentRegional> findAllByDirectorId(BigInteger directorId);
}
