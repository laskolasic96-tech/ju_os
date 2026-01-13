package com.example.ju_os.db;
import java.sql.*;
import java.util.*;
import com.example.ju_os.config.ConfigLoader;

public class OracleRepository {
    private final ConnectionPool pool = ConnectionPool.getInstance();
    public List<String> getAllTables() throws SQLException {
        List<String> r = new ArrayList<>();
        try (Connection c = pool.getConnection(); Statement s = c.createStatement();
             ResultSet rs = s.executeQuery("SELECT owner||'.'||table_name FROM all_tables WHERE owner NOT IN ('SYS','SYSTEM') ORDER BY 1")) {
            while (rs.next()) r.add(rs.getString(1));
        }
        return r;
    }
    public List<String> getAllViews() throws SQLException {
        List<String> r = new ArrayList<>();
        try (Connection c = pool.getConnection(); Statement s = c.createStatement();
             ResultSet rs = s.executeQuery("SELECT owner||'.'||view_name FROM all_views WHERE owner NOT IN ('SYS','SYSTEM') ORDER BY 1")) {
            while (rs.next()) r.add(rs.getString(1));
        }
        return r;
    }
    public List<String> getAllTablesAndViews() throws SQLException {
        List<String> r = new ArrayList<>();
        r.addAll(getAllTables());
        r.addAll(getAllViews());
        return r;
    }
    public List<Map<String,Object>> getTableData(String table, int offset, int limit) throws SQLException {
        var cfg = ConfigLoader.getInstance();
        limit = Math.min(limit, cfg.getMaxPageSize());
        List<Map<String,Object>> r = new ArrayList<>();
        String sql = "SELECT * FROM " + esc(table) + " OFFSET " + offset + " ROWS FETCH NEXT " + limit + " ROWS ONLY";
        try (Connection c = pool.getConnection(); Statement s = c.createStatement(); ResultSet rs = s.executeQuery(sql)) {
            ResultSetMetaData m = rs.getMetaData();
            while (rs.next()) {
                Map<String,Object> row = new LinkedHashMap<>();
                for (int i = 1; i <= m.getColumnCount(); i++) row.put(m.getColumnName(i), rs.getObject(i));
                r.add(row);
            }
        }
        return r;
    }
    public String getTableDataAsCsv(String table, int offset, int limit) throws SQLException {
        List<Map<String,Object>> d = getTableData(table, offset, limit);
        if (d.isEmpty()) return "";
        StringBuilder sb = new StringBuilder();
        List<String> h = new ArrayList<>(d.get(0).keySet());
        sb.append(String.join(",", h)).append("\n");
        for (Map<String,Object> row : d) {
            List<String> vals = new ArrayList<>();
            for (String k : h) {
                Object v = row.get(k);
                vals.add(v == null ? "" : (v.toString().contains(",") ? "\"" + v + "\"" : v.toString()));
            }
            sb.append(String.join(",", vals)).append("\n");
        }
        return sb.toString();
    }
    public Map<String,Object> getRowById(String table, String col, Object id) throws SQLException {
        String sql = "SELECT * FROM " + esc(table) + " WHERE " + esc(col) + " = ?";
        try (Connection c = pool.getConnection(); var ps = c.prepareStatement(sql)) {
            ps.setObject(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    ResultSetMetaData m = rs.getMetaData();
                    Map<String,Object> row = new LinkedHashMap<>();
                    for (int i = 1; i <= m.getColumnCount(); i++) row.put(m.getColumnName(i), rs.getObject(i));
                    return row;
                }
            }
        }
        return null;
    }
    public int updateRow(String table, String col, Object id, Map<String,Object> upd) throws SQLException {
        if (upd == null || upd.isEmpty()) return 0;
        StringBuilder set = new StringBuilder();
        List<Object> vals = new ArrayList<>();
        for (Map.Entry<String,Object> e : upd.entrySet()) {
            if (set.length() > 0) set.append(", ");
            set.append(esc(e.getKey())).append(" = ?");
            vals.add(e.getValue());
        }
        vals.add(id);
        String sql = "UPDATE " + esc(table) + " SET " + set + " WHERE " + esc(col) + " = ?";
        try (Connection c = pool.getConnection(); var ps = c.prepareStatement(sql)) {
            for (int i = 0; i < vals.size(); i++) ps.setObject(i + 1, vals.get(i));
            return ps.executeUpdate();
        }
    }
    public int deleteRow(String table, String col, Object id) throws SQLException {
        String sql = "DELETE FROM " + esc(table) + " WHERE " + esc(col) + " = ?";
        try (Connection c = pool.getConnection(); var ps = c.prepareStatement(sql)) {
            ps.setObject(1, id);
            return ps.executeUpdate();
        }
    }
    private String esc(String s) { return "\"" + s.replace("\"", "\"\"") + "\""; }
}
