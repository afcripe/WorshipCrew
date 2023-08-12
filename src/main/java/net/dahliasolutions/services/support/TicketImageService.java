package net.dahliasolutions.services.support;

import lombok.RequiredArgsConstructor;
import net.dahliasolutions.data.TicketImageRepository;
import net.dahliasolutions.models.support.TicketImage;
import net.dahliasolutions.services.support.TicketImageServiceInterface;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TicketImageService implements TicketImageServiceInterface {

    private final TicketImageRepository ticketImageRepository;

    @Override
    public TicketImage createStoredImage(TicketImage image) {
        return ticketImageRepository.save(image);
    }

    @Override
    public Optional<TicketImage> findById(BigInteger id) {
        return ticketImageRepository.findById(id);
    }

    @Override
    public Optional<TicketImage> findByName(String name) {
        return ticketImageRepository.findByName(name);
    }

    @Override
    public Optional<TicketImage> findByFileLocation(String fileLocation) {
        return ticketImageRepository.findByFileLocation(fileLocation);
    }

    @Override
    public List<TicketImage> findAll() {
        return ticketImageRepository.findAll();
    }

    @Override
    public List<TicketImage> findBySearchTerm(String searchTerm) {
        return ticketImageRepository.findBySearchTerm(searchTerm);
    }

    @Override
    public void save(TicketImage image) {
        ticketImageRepository.save(image);
    }

    @Override
    public void deleteById(BigInteger id) {
        ticketImageRepository.deleteById(id);
    }
}
