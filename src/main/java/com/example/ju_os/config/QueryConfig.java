package com.example.ju_os.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class QueryConfig {
    private static QueryConfig instance;
    private Map<String, QueryDefinition> queries = new LinkedHashMap<>();
    private String configPath = "queries.yaml";

    private QueryConfig() {
        loadConfig();
    }

    public static synchronized QueryConfig getInstance() {
        if (instance == null) instance = new QueryConfig();
        return instance;
    }

    private void loadConfig() {
        try {
            ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
            QueryConfigFile config = mapper.readValue(new FileInputStream(configPath), QueryConfigFile.class);
            
            if (config.queries != null) {
                for (QueryDefinition qd : config.queries) {
                    queries.put(qd.name, qd);
                }
            }
            System.out.println("Loaded " + queries.size() + " queries from " + configPath);
        } catch (IOException e) {
            System.err.println("Failed to load queries.yaml: " + e.getMessage());
        }
    }

    public QueryDefinition getQuery(String name) {
        return queries.get(name);
    }

    public List<String> getQueryNames() {
        return new ArrayList<>(queries.keySet());
    }

    public static class QueryConfigFile {
        public DatabaseConfig database;
        public List<QueryDefinition> queries;
    }

    public static class DatabaseConfig {
        public String url;
        public String username;
        public String password;
        public PoolConfig pool;
    }

    public static class PoolConfig {
        public int minSize = 5;
        public int maxSize = 20;
    }

    public static class QueryDefinition {
        public String name;
        public String type;  // select, insert, update, delete
        public String description;
        public String sql;
        public List<ParamDefinition> params;
    }

    public static class ParamDefinition {
        public String name;
        public String type;  // string, integer, double, boolean
        public boolean required = false;
        public Object defaultValue;
    }

    public void reload() {
        queries.clear();
        loadConfig();
    }
}
