package net.dahliasolutions.data;

import net.dahliasolutions.models.campus.Campus;
import net.dahliasolutions.models.department.DepartmentCampus;
import net.dahliasolutions.models.department.DepartmentRegional;
import org.springframework.data.jpa.repository.JpaRepository;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;

public interface DepartmentCampusRepository extends JpaRepository<DepartmentCampus, BigInteger> {

    Optional<DepartmentCampus> findByNameAndCampus(String departmentName, Campus campus);
    List<DepartmentCampus> findAllByDirectorId(BigInteger directorId);
    List<DepartmentCampus> findAllByRegionalDepartment(DepartmentRegional departmentRegional);
    List<DepartmentCampus> findAllByCampus(Campus campus);
    List<DepartmentCampus> findDepartmentCampusesByRegionalDepartment(DepartmentRegional departmentRegional);

}
