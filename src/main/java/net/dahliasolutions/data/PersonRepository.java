package net.dahliasolutions.data;

import net.dahliasolutions.models.Person;
import org.springframework.data.jpa.repository.JpaRepository;

import java.math.BigInteger;

public interface PersonRepository extends JpaRepository<Person, BigInteger> {

}
