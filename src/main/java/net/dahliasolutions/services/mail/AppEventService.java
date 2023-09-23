package net.dahliasolutions.services.mail;

import lombok.RequiredArgsConstructor;
import net.dahliasolutions.data.AppEventRepository;
import net.dahliasolutions.models.*;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AppEventService implements AppEventServiceInterface{

    private final AppEventRepository appEventRepository;

    @Override
    public Optional<AppEvent> findAppEventById(BigInteger id) {
        return appEventRepository.findById(id);
    }

    @Override
    public void save(AppEvent appEvent) {
        appEventRepository.save(appEvent);
    }
}
