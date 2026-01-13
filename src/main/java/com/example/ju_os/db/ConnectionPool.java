package com.example.ju_os.db;
import java.sql.*;
import java.util.*;

public class ConnectionPool {
    private static ConnectionPool instance;
    private final List<Connection> pool = new ArrayList<>();
    private final String url, user, pass;
    private final int maxSize = 20;
    private ConnectionPool() {
        var c = com.example.ju_os.config.ConfigLoader.getInstance();
        this.url = c.getDbUrl(); this.user = c.getDbUsername(); this.pass = c.getDbPassword();
        try { Class.forName("oracle.jdbc.OracleDriver");
            for (int i = 0; i < 5; i++) pool.add(DriverManager.getConnection(url, user, pass));
        } catch (Exception e) { System.err.println("Pool init failed: " + e.getMessage()); }
    }
    public static synchronized ConnectionPool getInstance() {
        if (instance == null) instance = new ConnectionPool();
        return instance;
    }
    public synchronized Connection getConnection() {
        while (pool.isEmpty()) {
            if (pool.size() < maxSize) {
                try { pool.add(DriverManager.getConnection(url, user, pass)); }
                catch (SQLException e) { throw new RuntimeException(e); }
            } else {
                try { wait(1000); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            }
        }
        return pool.remove(pool.size() - 1);
    }
    public synchronized void releaseConnection(Connection c) {
        if (c != null) {
            try { if (c.isValid(5)) pool.add(c); else c.close(); }
            catch (SQLException e) { System.err.println(e.getMessage()); }
        }
    }
    public synchronized int getActiveConnections() { return pool.size(); }
}
