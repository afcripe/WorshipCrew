package net.dahliasolutions.data;

import net.dahliasolutions.models.StoredImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.math.BigInteger;
import java.util.Optional;

public interface StoredImageRepository extends JpaRepository<StoredImage, BigInteger> {

    Optional<StoredImage> findByName(String imageName);
    Optional<StoredImage> findByFileLocation(String fileLocation);

}
