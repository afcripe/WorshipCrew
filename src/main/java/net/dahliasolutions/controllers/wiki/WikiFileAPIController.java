package net.dahliasolutions.controllers.wiki;

import lombok.RequiredArgsConstructor;
import net.dahliasolutions.models.AppServer;
import net.dahliasolutions.models.store.FileUploadModel;
import net.dahliasolutions.models.wiki.WikiImage;
import net.dahliasolutions.services.RedirectService;
import net.dahliasolutions.services.wiki.WikiImageService;
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
@RequestMapping("/api/v1/contentmanager/wiki")
@RequiredArgsConstructor
public class WikiFileAPIController {

    private final WikiImageService wikiImageService;
    private final RedirectService redirectService;
    private final AppServer appServer;

    @GetMapping("")
    public List<WikiImage> goFindAllWikiImages() {
        return wikiImageService.findAll();
    }

    @GetMapping("/images")
    public List<WikiImage> getAllWikiImages() {
        return wikiImageService.findAll();
    }

    @GetMapping("/images/{searchTerm}")
    public List<WikiImage> searchWikiImages(@PathVariable String searchTerm) {
        return wikiImageService.findBySearchTerm(searchTerm);
    }

    @PostMapping("/uploadimage")
    public WikiImage uploadNewWikiImage(@ModelAttribute FileUploadModel fileUploadModel, @RequestPart("imageFile") MultipartFile imageFile) {
        // ToDo - save multipart file to disk and return relative URL location

        // get the image save location and verify directories
        String fileName = StringUtils.cleanPath(imageFile.getOriginalFilename());
        String uploadDir = appServer.getResourceDir()+"/wiki/images";
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
        String fileURL = appServer.getResourceURL()+"/wiki/images/"+fileName;
        filePath = uploadPath.resolve(fileName);

        // try to save file
        try {
            InputStream inputStream = imageFile.getInputStream();
            Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e){
            System.out.println("error saving file");
        }

        WikiImage wikiImage = new WikiImage(null, fileUploadModel.fileName(), fileUploadModel.fileDescription(), fileURL);

        return wikiImageService.createStoredImage(wikiImage);
    }

    @GetMapping("/removeimage/{id}")
    public String removeStoredWikiIamge(@PathVariable BigInteger id){
        Optional<WikiImage> storedImage = wikiImageService.findById(id);
        if (storedImage.isPresent()) {
            String trimmedPath = appServer.getResourceDir().replace("/content", "");
            Path imagePath = Paths.get(trimmedPath+storedImage.get().getFileLocation());
            try {
                Files.deleteIfExists(imagePath);
                System.out.println("Deleting File");
            } catch (IOException ioException) {
                System.out.println("File Not Found");
            }
            wikiImageService.deleteById(id);
        }
        return "";
    }
}
