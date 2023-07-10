package net.dahliasolutions.models;


public class AppServer {
    private String baseURL;

    public String getBaseURL() {
        return baseURL;
    }

    public void setBaseURL() {
        this.baseURL = "http://localhost:8080";
    }

    public void setBaseURL(String baseURL) {
        this.baseURL = baseURL;
    }

    public AppServer(String baseURL) {
        this.baseURL = baseURL;
    }
}
