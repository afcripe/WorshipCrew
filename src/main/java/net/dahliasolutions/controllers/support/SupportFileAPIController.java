package net.dahliasolutions.controllers.support;

import lombok.RequiredArgsConstructor;
import net.dahliasolutions.models.AppServer;
import net.dahliasolutions.models.records.SingleBigIntegerModel;
import net.dahliasolutions.models.store.FileUploadModel;
import net.dahliasolutions.models.support.TicketImage;
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
import java.util.ArrayList;
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

    @PostMapping("/uploadonlyimage")
    public TicketImage uploadOnlyImage(@RequestPart("imageFile") MultipartFile imageFile) {
        // get the image save location and verify directories
        String fileName = StringUtils.cleanPath(imageFile.getOriginalFilename());
        String uploadDir = appServer.getResourceDir() + "/support/images";
        String fileURL = appServer.getResourceURL() + "/support/images/" + fileName;
        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            try {
                Files.createDirectories(uploadPath);
            } catch (IOException e) {
                System.out.println("error creating dir");
            }
        }

        // try to save file
        try {
            InputStream inputStream = imageFile.getInputStream();
            Path filePath = uploadPath.resolve(fileName);
            Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            System.out.println("error saving file");
        }

        TicketImage ticketImage = new TicketImage(null, fileName, fileName, fileURL);

        return ticketImageService.createStoredImage(ticketImage);
    }

    @PostMapping("/uploadmultipleimages")
    public List<TicketImage> uploadImages(@RequestPart("imageFiles") MultipartFile[] imageFiles) {
        List<TicketImage> ticketImageList = new ArrayList<>();

        for (MultipartFile imageFile : imageFiles) {
            // get the image save location and verify directories
            String fileName = StringUtils.cleanPath(imageFile.getOriginalFilename());
            String uploadDir = appServer.getResourceDir() + "/support/images";
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                try {
                    Files.createDirectories(uploadPath);
                } catch (IOException e) {
                    System.out.println("error creating dir");
                }
            }

            // check for existing file
            Path filePath = uploadPath.resolve(fileName);
            if (Files.exists(filePath)) {
                String[] tokens = fileName.split("\\.(?=[^\\.]+$)");
                String milli = String.valueOf(System.currentTimeMillis());
                String suffix = milli.substring(milli.length()-4);
                fileName = tokens[tokens.length-2]+"-"+suffix+"."+tokens[tokens.length-1];
            }
            String fileURL = appServer.getResourceURL()+"/support/images/"+fileName;
            filePath = uploadPath.resolve(fileName);

            // try to save file
            try {
                InputStream inputStream = imageFile.getInputStream();
                Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                System.out.println("error saving file");
            }

            TicketImage ticketImage = new TicketImage(null, fileName, fileName, fileURL);
            ticketImageList.add(ticketImageService.createStoredImage(ticketImage));
        }

        return  ticketImageList;
    }

    @PostMapping("/deleteonlyimage")
    public SingleBigIntegerModel removeStoredImage(@ModelAttribute SingleBigIntegerModel image){
        Optional<TicketImage> ticketImage = ticketImageService.findById(image.id());
        if (ticketImage.isPresent()) {
            String trimmedPath = appServer.getResourceDir().replace("/content", "");
            Path imagePath = Paths.get(trimmedPath+ticketImage.get().getFileLocation());
            try {
                Files.deleteIfExists(imagePath);
                System.out.println("Deleting File");
            } catch (IOException ioException) {
                System.out.println("File Not Found");
            }
            ticketImageService.deleteById(ticketImage.get().getId());
        }
        return image;
    }

}
