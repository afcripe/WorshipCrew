package net.dahliasolutions.services.user;

import lombok.RequiredArgsConstructor;
import net.dahliasolutions.data.EndpointRepository;
import net.dahliasolutions.models.user.User;
import net.dahliasolutions.models.user.UserEndpoint;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class EndpointService implements EndpointServiceInterface{

    private final EndpointRepository endpointRepository;

    @Override
    public UserEndpoint save(UserEndpoint endpoint) {
        return endpointRepository.save(endpoint);
    }

    @Override
    public List<UserEndpoint> findAllByUser(User user) {
        return endpointRepository.findAllByUser(user);
    }

    @Override
    public Optional<UserEndpoint> findByToken(String token) {
        return endpointRepository.findByToken(token);
    }

    @Override
    public void deletByToken(String token) {
        endpointRepository.deleteByToken(token);
    }
}
