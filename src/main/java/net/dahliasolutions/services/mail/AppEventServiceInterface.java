package net.dahliasolutions.services.mail;

import net.dahliasolutions.models.AppEvent;

import java.math.BigInteger;
import java.util.Optional;

public interface AppEventServiceInterface {

    Optional<AppEvent> findAppEventById(BigInteger id);
    void save(AppEvent appEvent);

}
