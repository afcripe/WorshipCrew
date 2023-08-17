package net.dahliasolutions.data;

import net.dahliasolutions.models.Message;
import org.springframework.data.jpa.repository.JpaRepository;

import java.math.BigInteger;

public interface MessageRepository extends JpaRepository<Message, BigInteger> {

}
