package net.dahliasolutions.services;

import lombok.RequiredArgsConstructor;
import net.dahliasolutions.data.PersonRepository;
import net.dahliasolutions.models.Person;
import net.dahliasolutions.models.User;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PersonService implements PersonServiceInterface{

    private final PersonRepository personRepository;

    @Override
    public Person createPerson(User user) {
        String personName = user.getFirstName() + " " + user.getLastName();
        Person person = new Person(user.getId(), personName, user.getContactEmail());
        return personRepository.save(person);
    }

    @Override
    public void updatePerson(User user) {
        String personName = user.getFirstName() + " " + user.getLastName();
        Person person = new Person(user.getId(), personName, user.getContactEmail());
        personRepository.save(person);
    }

    @Override
    public Person getPersonFromUser(User user) {
        String personName = user.getFirstName() + " " + user.getLastName();
        return new Person(user.getId(), personName, user.getContactEmail());
    }

    @Override
    public Optional<Person> getPersonById(BigInteger id) {
        return personRepository.findById(id);
    }

    @Override
    public List<Person> findAll() {
        return personRepository.findAll();
    }
}
