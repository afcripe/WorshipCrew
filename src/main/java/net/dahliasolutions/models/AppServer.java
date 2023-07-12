package net.dahliasolutions.models;


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
}
