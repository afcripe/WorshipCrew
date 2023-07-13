package net.dahliasolutions.controllers;

import lombok.RequiredArgsConstructor;
import net.dahliasolutions.models.*;
import net.dahliasolutions.services.*;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;

@RestController
@RequestMapping("/api/v1/contentmanager")
@RequiredArgsConstructor
public class FileAPIController {

    private final StoredImageService storedImageService;
    private final RedirectService redirectService;
    private final AppServer appServer;

    @GetMapping("")
    public List<StoredImage> goFindAll() {
        return storedImageService.findAll();
    }

    @GetMapping("/images")
    public List<StoredImage> getAllImages() {
        return storedImageService.findAll();
    }

    @GetMapping("/images/{searchTerm}")
    public List<StoredImage> searchImages(@PathVariable String searchTerm) {
        return storedImageService.findBySearchTerm(searchTerm);
    }

    @PostMapping("/uploadimage")
    public StoredImage uploadNewImage(@ModelAttribute FileUploadModel fileUploadModel, @RequestPart("imageFile") MultipartFile imageFile) {
        // ToDo - save multipart file to disk and return relative URL location

        // get the image save location and verify directories
        String fileName = StringUtils.cleanPath(imageFile.getOriginalFilename());
        String uploadDir = appServer.getResourceDir()+"/images";
        String fileURL = appServer.getResourceURL()+"/images/"+fileName;
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

        StoredImage storedImage = new StoredImage(null, fileUploadModel.fileName(), fileUploadModel.fileDescription(), fileURL);

        return storedImageService.createStoredImage(storedImage);
    }

}
