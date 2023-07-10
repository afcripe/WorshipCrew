package net.dahliasolutions.services;

import net.dahliasolutions.models.Person;
import net.dahliasolutions.models.User;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;

public interface PersonServiceInterface {

    Person createPerson(User user);
    void updatePerson(User user);
    Person getPersonFromUser(User user);
    Optional<Person> getPersonById(BigInteger id);
    List<Person> findAll();
}
