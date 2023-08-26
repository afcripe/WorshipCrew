package net.dahliasolutions.models;


import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

public class AppServer {
    private String baseURL;
    private String resourceDir;
    private String resourceURL;

    public String getBaseURL() {
        return baseURL;
    }

    public String getResourceDir() { return resourceDir; }

    public String getResourceURL() { return resourceURL; }

    public void setBaseURL() {
        this.baseURL = "http://localhost:8081";
    }

    public void setBaseURL(String baseURL) {
        this.baseURL = baseURL;
    }

    public void setResourceDir() {
        this.resourceDir = "/var/destinyworshipexchange/content/";
    }

    public void setResourceDir(String resourceDir) {
        this.resourceDir = resourceDir;
    }

    public void setResourceURL() {
        this.resourceURL = "/content";
    }

    public void setResourceURL(String resourceURL) {
        this.resourceURL = resourceURL;
    }

    public AppServer(String baseURL, String resourceDir, String resourceURL) {
        this.baseURL = baseURL;
        this.resourceDir = resourceDir;
        this.resourceURL = resourceURL;
    }
    public void setStaticFiles(boolean overWright) {
        // load resources files
        File resStyleDefault = new File(getClass().getResource("/static/css/style.css").getFile());
        File resStyleDark = new File(getClass().getResource("/static/css/dark.css").getFile());
        File resFavIcon = new File(getClass().getResource("/static/img/favicon.ico").getFile());
        File resFavPng = new File(getClass().getResource("/static/img/favicon.png").getFile());

        // load content files
        File cntStyleDefault = new File(getResourceDir()+"/static/css/style.css");
        File cntStyleDark = new File(getResourceDir()+"/static/css/dark.css");
        File cntFavIcon = new File(getResourceDir()+"/static/img/favicon.ico");
        File cntFavPng = new File(getResourceDir()+"/static/img/favicon.png");

        // create content dir if needed
        Path contentCSSPath = Paths.get(getResourceDir()+"/static/css");
        if (!Files.exists(contentCSSPath)) {
            try {
                Files.createDirectories(contentCSSPath);
            } catch (IOException e){
                System.out.println("error creating dir");
            }
        }
        Path contentIMGPath = Paths.get(getResourceDir()+"/static/img");
        if (!Files.exists(contentIMGPath)) {
            try {
                Files.createDirectories(contentIMGPath);
            } catch (IOException e){
                System.out.println("error creating dir");
            }
        }

        try {
            copyStaticFile(resStyleDefault, cntStyleDefault, overWright);
        } catch (IOException ioException) {
            System.out.println(ioException);
        }

        try {
            copyStaticFile(resStyleDark, cntStyleDark, overWright);
        } catch (IOException ioException) {
            System.out.println(ioException);
        }

        try {
            copyStaticFile(resFavIcon, cntFavIcon, overWright);
        } catch (IOException ioException) {
            System.out.println(ioException);
        }

        try {
            copyStaticFile(resFavPng, cntFavPng, overWright);
        } catch (IOException ioException) {
            System.out.println(ioException);
        }
    }

    private void copyStaticFile(File source, File target, boolean overWright) throws IOException {
        // check of source exists
        if (!source.exists()) { return; }

        // check if target exists
        if (overWright) {
            if (target.exists()) {
                target.delete();
            }
        }

        // copy file if target not exist
        if (!target.exists()) {
            String pathOnly = target.toPath().toString().replace(target.getName(), "");
            Path targetPath = Paths.get(pathOnly);
            Path filePath = targetPath.resolve(target.getName());
            Files.copy(source.toPath(), filePath);
        }

    }
}
