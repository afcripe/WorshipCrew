package net.dahliasolutions.controllers.support;

import lombok.RequiredArgsConstructor;
import net.dahliasolutions.models.AppServer;
import net.dahliasolutions.models.store.FileUploadModel;
import net.dahliasolutions.models.store.StoreImage;
import net.dahliasolutions.models.support.TicketImage;
import net.dahliasolutions.services.RedirectService;
import net.dahliasolutions.services.store.StoreImageService;
import net.dahliasolutions.services.support.TicketImageService;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/contentmanager/support")
@RequiredArgsConstructor
public class SupportFileAPIController {

    private final TicketImageService ticketImageService;
    private final AppServer appServer;

    @GetMapping("")
    public List<TicketImage> goFindAllTicketImages() {
        return ticketImageService.findAll();
    }

    @GetMapping("/images")
    public List<TicketImage> getAllTicketImages() {
        return ticketImageService.findAll();
    }

    @GetMapping("/images/{searchTerm}")
    public List<TicketImage> searchTicketImages(@PathVariable String searchTerm) {
        return ticketImageService.findBySearchTerm(searchTerm);
    }

    @PostMapping("/uploadimage")
    public TicketImage uploadNewImage(@ModelAttribute FileUploadModel fileUploadModel, @RequestPart("imageFile") MultipartFile imageFile) {
        // ToDo - save multipart file to disk and return relative URL location

        // get the image save location and verify directories
        String fileName = StringUtils.cleanPath(imageFile.getOriginalFilename());
        String uploadDir = appServer.getResourceDir()+"/support/images";
        String fileURL = appServer.getResourceURL()+"/support/images/"+fileName;
        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            try {
                Files.createDirectories(uploadPath);
            } catch (IOException e){
                System.out.println("error creating dir");
            }
        }

        // try to save file
        try {
            InputStream inputStream = imageFile.getInputStream();
            Path filePath = uploadPath.resolve(fileName);
            Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e){
            System.out.println("error saving file");
        }

        TicketImage ticketImage = new TicketImage(null, fileUploadModel.fileName(), fileUploadModel.fileDescription(), fileURL);

        return ticketImageService.createStoredImage(ticketImage);
    }

    @GetMapping("/removeimage/{id}")
    public String removeStoredIamge(@PathVariable BigInteger id){
        Optional<TicketImage> ticketImage = ticketImageService.findById(id);
        if (ticketImage.isPresent()) {
            ticketImageService.deleteById(id);
        }
        return "";
    }

}
