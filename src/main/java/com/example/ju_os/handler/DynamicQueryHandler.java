package com.example.ju_os.handler;

import com.example.ju_os.config.QueryConfig;
import com.example.ju_os.db.QueryExecutor;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;
import java.util.*;

public class DynamicQueryHandler implements HttpHandler {
    private final ObjectMapper mapper = new ObjectMapper();
    private final QueryExecutor executor = new QueryExecutor();

    public void handleRequest(HttpServerExchange exchange) throws Exception {
        String path = exchange.getRequestPath();
        String method = exchange.getRequestMethod().toString();
        String apiPrefix = QueryConfig.getInstance().getQueryNames().isEmpty() ? 
            "/api/v1" : "/api/v1/query";

        // Parse path: /api/v1/query/{queryName}
        String queryPath = path.substring(apiPrefix.length());
        if (!queryPath.startsWith("/")) queryPath = "/" + queryPath;
        
        String[] parts = queryPath.split("/");
        if (parts.length < 3) {
            sendError(exchange, "Invalid path. Use: /api/v1/query/{queryName}", 400);
            return;
        }
        String queryName = parts[2];

        QueryConfig.QueryDefinition qd = QueryConfig.getInstance().getQuery(queryName);
        if (qd == null) {
            sendError(exchange, "Query not found: " + queryName, 404);
            return;
        }

        // Check method matches query type
        if (!isMethodAllowed(qd.type, method)) {
            sendError(exchange, "Method " + method + " not allowed for " + qd.type + " query", 405);
            return;
        }

        try {
            Map<String, Object> params = extractParams(exchange, qd);

            Map<String, Object> result = new LinkedHashMap<>();
            result.put("query", queryName);
            result.put("type", qd.type);

            if ("select".equalsIgnoreCase(qd.type)) {
                List<Map<String, Object>> data = executor.executeSelect(queryName, params);
                result.put("success", true);
                result.put("data", data);
                result.put("rowCount", data.size());
                sendJson(exchange, result, 200);
            } else if ("insert".equalsIgnoreCase(qd.type)) {
                Map<String, Object> insertResult = executor.executeInsertReturn(queryName, params);
                result.put("success", true);
                result.put("result", insertResult);
                sendJson(exchange, result, 200);
            } else {
                int affected = executor.executeUpdate(queryName, params);
                result.put("success", true);
                result.put("affectedRows", affected);
                sendJson(exchange, result, 200);
            }
        } catch (IllegalArgumentException e) {
            sendError(exchange, e.getMessage(), 400);
        } catch (Exception e) {
            sendError(exchange, "Query execution failed: " + e.getMessage(), 500);
        }
    }

    private boolean isMethodAllowed(String queryType, String method) {
        switch (queryType.toLowerCase()) {
            case "select": return "GET".equalsIgnoreCase(method);
            case "insert": return "POST".equalsIgnoreCase(method);
            case "update": return "PUT".equalsIgnoreCase(method) || "POST".equalsIgnoreCase(method);
            case "delete": return "DELETE".equalsIgnoreCase(method);
            default: return false;
        }
    }

    private Map<String, Object> extractParams(HttpServerExchange exchange, QueryConfig.QueryDefinition qd) {
        Map<String, Object> params = new LinkedHashMap<>();
        Map<String, Deque<String>> queryParams = exchange.getQueryParameters();

        // Add query parameters
        for (String key : queryParams.keySet()) {
            params.put(key, queryParams.get(key).getFirst());
        }

        // For POST/PUT with body, merge with query params
        if ("POST".equalsIgnoreCase(exchange.getRequestMethod().toString()) || 
            "PUT".equalsIgnoreCase(exchange.getRequestMethod().toString())) {
            try {
                String body = exchange.getInputStream().read().toString();
                if (body != null && !body.isEmpty()) {
                    Map<String, Object> bodyParams = mapper.readValue(body, Map.class);
                    params.putAll(bodyParams);
                }
            } catch (Exception e) {
                // Ignore body parse errors
            }
        }

        return params;
    }

    private void sendJson(HttpServerExchange exchange, Map<String, Object> data, int status) {
        try {
            exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "application/json");
            exchange.setStatusCode(status);
            exchange.getResponseSender().send(mapper.writeValueAsString(data));
        } catch (Exception e) {
            exchange.setStatusCode(500);
            exchange.getResponseSender().send("{\"error\":\"Serialization failed\"}");
        }
    }

    private void sendError(HttpServerExchange exchange, String message, int status) {
        sendJson(exchange, Map.of("success", false, "error", message), status);
    }
}
