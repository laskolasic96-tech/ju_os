package com.example.ju_os.config;
import java.io.*;
import java.util.Properties;

public class ConfigLoader {
    private static ConfigLoader instance;
    private final Properties props = new Properties();
    private ConfigLoader() {
        String[] paths = {"config.yaml", "config.properties"};
        for (String path : paths) {
            try (InputStream in = new FileInputStream(path)) {
                props.load(in);
                System.out.println("Loaded: " + path);
                return;
            } catch (IOException e) {}
        }
    }
    public static synchronized ConfigLoader getInstance() {
        if (instance == null) instance = new ConfigLoader();
        return instance;
    }
    public String getDbUrl() { return props.getProperty("db.url", "jdbc:oracle:thin:@localhost:1521/XEPDB1"); }
    public String getDbUsername() { return props.getProperty("db.username", "system"); }
    public String getDbPassword() { return props.getProperty("db.password", "oracle"); }
    public int getServerPort() { return Integer.parseInt(props.getProperty("server.port", "8080")); }
    public String getServerHost() { return props.getProperty("server.host", "0.0.0.0"); }
    public String getApiPrefix() { return props.getProperty("api.prefix", "/api/v1"); }
    public int getDefaultPageSize() { return Integer.parseInt(props.getProperty("api.defaultPageSize", "100")); }
    public int getMaxPageSize() { return Integer.parseInt(props.getProperty("api.maxPageSize", "1000")); }
}
