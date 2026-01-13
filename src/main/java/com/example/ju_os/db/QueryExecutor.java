package com.example.ju_os.db;

import com.example.ju_os.config.QueryConfig;
import com.example.ju_os.config.QueryConfig.QueryDefinition;

import java.sql.*;
import java.util.*;

public class QueryExecutor {
    private final ConnectionPool pool;

    public QueryExecutor() {
        this.pool = ConnectionPool.getInstance();
    }

    public List<Map<String, Object>> executeSelect(String queryName, Map<String, Object> params) {
        QueryDefinition qd = QueryConfig.getInstance().getQuery(queryName);
        if (qd == null) {
            throw new IllegalArgumentException("Query not found: " + queryName);
        }
        if (!"select".equalsIgnoreCase(qd.type)) {
            throw new IllegalArgumentException("Query is not a SELECT: " + queryName);
        }

        QueryParser.ParsedQuery pq = QueryParser.parse(qd, params);
        List<Map<String, Object>> result = new ArrayList<>();

        try (Connection conn = pool.getConnection();
             PreparedStatement ps = buildStatement(conn, pq)) {
            
            try (ResultSet rs = ps.executeQuery()) {
                ResultSetMetaData meta = rs.getMetaData();
                int count = meta.getColumnCount();
                
                while (rs.next()) {
                    Map<String, Object> row = new LinkedHashMap<>();
                    for (int i = 1; i <= count; i++) {
                        row.put(meta.getColumnName(i), rs.getObject(i));
                    }
                    result.add(row);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Query execution failed: " + e.getMessage(), e);
        }

        return result;
    }

    public int executeUpdate(String queryName, Map<String, Object> params) {
        QueryDefinition qd = QueryConfig.getInstance().getQuery(queryName);
        if (qd == null) {
            throw new IllegalArgumentException("Query not found: " + queryName);
        }
        if (!"update".equalsIgnoreCase(qd.type) && !"delete".equalsIgnoreCase(qd.type) && !"insert".equalsIgnoreCase(qd.type)) {
            throw new IllegalArgumentException("Query is not an UPDATE/DELETE/INSERT: " + queryName);
        }

        QueryParser.ParsedQuery pq = QueryParser.parse(qd, params);

        try (Connection conn = pool.getConnection();
             PreparedStatement ps = buildStatement(conn, pq)) {
            return ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Query execution failed: " + e.getMessage(), e);
        }
    }

    public Map<String, Object> executeInsertReturn(String queryName, Map<String, Object> params) {
        QueryDefinition qd = QueryConfig.getInstance().getQuery(queryName);
        if (qd == null) {
            throw new IllegalArgumentException("Query not found: " + queryName);
        }
        if (!"insert".equalsIgnoreCase(qd.type)) {
            throw new IllegalArgumentException("Query is not an INSERT: " + queryName);
        }

        QueryParser.ParsedQuery pq = QueryParser.parse(qd, params);

        try (Connection conn = pool.getConnection();
             PreparedStatement ps = buildStatement(conn, pq, true)) {
            int affected = ps.executeUpdate();
            
            Map<String, Object> result = new LinkedHashMap<>();
            result.put("affectedRows", affected);
            
            // Try to get generated keys
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    result.put("generatedId", keys.getObject(1));
                }
            }
            
            return result;
        } catch (SQLException e) {
            throw new RuntimeException("Query execution failed: " + e.getMessage(), e);
        }
    }

    private PreparedStatement buildStatement(Connection conn, QueryParser.ParsedQuery pq) throws SQLException {
        return buildStatement(conn, pq, false);
    }

    private PreparedStatement buildStatement(Connection conn, QueryParser.ParsedQuery pq, boolean returnKeys) throws SQLException {
        PreparedStatement ps;
        if (returnKeys) {
            ps = conn.prepareStatement(pq.sql, Statement.RETURN_GENERATED_KEYS);
        } else {
            ps = conn.prepareStatement(pq.sql);
        }

        int idx = 1;
        for (QueryParser.NamedParam np : pq.params) {
            ps.setObject(idx++, convertValue(np.value, np.type));
        }

        return ps;
    }

    private Object convertValue(Object value, String type) {
        if (value == null) return null;
        
        String typeLower = type.toLowerCase();
        String strValue = value.toString();
        
        switch (typeLower) {
            case "integer":
            case "int":
                if (value instanceof Number) return value;
                try { return Integer.parseInt(strValue); } catch (NumberFormatException e) { return value; }
            case "long":
                try { return Long.parseLong(strValue); } catch (NumberFormatException e) { return value; }
            case "double":
            case "float":
                if (value instanceof Number) return value;
                try { return Double.parseDouble(strValue); } catch (NumberFormatException e) { return value; }
            case "boolean":
                return Boolean.parseBoolean(strValue);
            case "string":
            default:
                return strValue;
        }
    }
}
