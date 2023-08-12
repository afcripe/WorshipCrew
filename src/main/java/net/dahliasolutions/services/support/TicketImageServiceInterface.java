package net.dahliasolutions.services.support;

import net.dahliasolutions.models.support.TicketImage;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;

public interface TicketImageServiceInterface {

    TicketImage createStoredImage(TicketImage image);
    Optional<TicketImage> findById(BigInteger id);
    Optional<TicketImage> findByName(String name);
    Optional<TicketImage> findByFileLocation(String fileLocation);
    List<TicketImage> findAll();
    List<TicketImage> findBySearchTerm(String searchTerm);
    void save(TicketImage image);
    void deleteById(BigInteger id);

}
