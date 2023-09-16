package net.dahliasolutions.controllers.store;

import lombok.RequiredArgsConstructor;
import net.dahliasolutions.models.*;
import net.dahliasolutions.models.store.FileUploadModel;
import net.dahliasolutions.models.store.StoreImage;
import net.dahliasolutions.services.*;
import net.dahliasolutions.services.store.StoreImageService;
import org.hibernate.mapping.IdentifierBag;
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
@RequestMapping("/api/v1/contentmanager/store")
@RequiredArgsConstructor
public class StoreFileAPIController {

    private final StoreImageService storedImageService;
    private final RedirectService redirectService;
    private final AppServer appServer;

    @GetMapping("")
    public List<StoreImage> goFindAll() {
        return storedImageService.findAll();
    }

    @GetMapping("/images")
    public List<StoreImage> getAllImages() {
        return storedImageService.findAll();
    }

    @GetMapping("/images/{searchTerm}")
    public List<StoreImage> searchImages(@PathVariable String searchTerm) {
        return storedImageService.findBySearchTerm(searchTerm);
    }

    @PostMapping("/uploadimage")
    public StoreImage uploadNewImage(@ModelAttribute FileUploadModel fileUploadModel, @RequestPart("imageFile") MultipartFile imageFile) {
        // ToDo - save multipart file to disk and return relative URL location

        // get the image save location and verify directories
        String fileName = StringUtils.cleanPath(imageFile.getOriginalFilename());
        String uploadDir = appServer.getResourceDir()+"/store/images";
        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            try {
                Files.createDirectories(uploadPath);
            } catch (IOException e){
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
        String fileURL = appServer.getResourceURL()+"/store/images/"+fileName;
        filePath = uploadPath.resolve(fileName);

        // try to save file
        try {
            InputStream inputStream = imageFile.getInputStream();
            Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e){
            System.out.println("error saving file");
        }

        StoreImage storeImage = new StoreImage(null, fileUploadModel.fileName(), fileUploadModel.fileDescription(), fileURL);

        return storedImageService.createStoredImage(storeImage);
    }

    @GetMapping("/removeimage/{id}")
    public String removeStoredIamge(@PathVariable BigInteger id){
        Optional<StoreImage> storedImage = storedImageService.findById(id);
        if (storedImage.isPresent()) {
            String trimmedPath = appServer.getResourceDir().replace("/content", "");
            Path imagePath = Paths.get(trimmedPath+storedImage.get().getFileLocation());
            try {
                Files.deleteIfExists(imagePath);
                System.out.println("Deleting File");
            } catch (IOException ioException) {
                System.out.println("File Not Found");
            }
            storedImageService.deleteById(id);
        }
        return "";
    }

}
