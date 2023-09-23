package net.dahliasolutions.data;

import net.dahliasolutions.models.AppEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.math.BigInteger;

public interface AppEventRepository extends JpaRepository<AppEvent, BigInteger> {

}
