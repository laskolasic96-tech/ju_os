package com.example.ju_os.handler;
import com.example.ju_os.db.OracleRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;
import java.sql.SQLException;
import java.util.*;

public class RowHandler implements HttpHandler {
    private final OracleRepository repo = new OracleRepository();
    private final ObjectMapper mapper = new ObjectMapper();
    public void handleRequest(HttpServerExchange ex) throws Exception {
        String path = ex.getRequestPath();
        String apiPrefix = com.example.ju_os.config.ConfigLoader.getInstance().getApiPrefix();
        path = path.substring(apiPrefix.length());
        try {
            Map<String,Object> resp = new HashMap<>();
            if (path.equals("/tables") || path.equals("/tables/")) {
                resp.put("success", true); resp.put("data", repo.getAllTables()); resp.put("message", "Tables");
            } else if (path.equals("/views") || path.equals("/views/")) {
                resp.put("success", true); resp.put("data", repo.getAllViews()); resp.put("message", "Views");
            } else if (path.equals("/all") || path.equals("/all/")) {
                resp.put("success", true); resp.put("data", repo.getAllTablesAndViews()); resp.put("message", "All");
            } else {
                resp.put("success", false); resp.put("message", "Not found"); resp.put("status", 404);
            }
            send(ex, resp);
        } catch (SQLException e) { send(ex, Map.of("success", false, "message", e.getMessage(), "status", 500)); }
    }
    protected void send(HttpServerExchange ex, Map<String,Object> resp) {
        try {
            ex.getResponseHeaders().put(Headers.CONTENT_TYPE, "application/json");
            ex.setStatusCode(resp.containsKey("status") ? (Integer)resp.get("status") : 200);
            ex.getResponseSender().send(mapper.writeValueAsString(resp));
        } catch (Exception e) { ex.setStatusCode(500); }
    }
}
