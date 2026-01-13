package com.example.ju_os.handler;

import com.example.ju_os.config.QueryConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;
import java.util.*;

public class QueriesListHandler implements HttpHandler {
    private final ObjectMapper mapper = new ObjectMapper();

    public void handleRequest(HttpServerExchange exchange) throws Exception {
        QueryConfig config = QueryConfig.getInstance();
        List<Map<String, Object>> queries = new ArrayList<>();

        for (String name : config.getQueryNames()) {
            QueryConfig.QueryDefinition qd = config.getQuery(name);
            Map<String, Object> qinfo = new LinkedHashMap<>();
            qinfo.put("name", qd.name);
            qinfo.put("type", qd.type);
            qinfo.put("description", qd.description);
            
            // Extract parameter names and types
            List<Map<String, Object>> params = new ArrayList<>();
            if (qd.params != null) {
                for (QueryConfig.ParamDefinition pd : qd.params) {
                    Map<String, Object> p = new LinkedHashMap<>();
                    p.put("name", pd.name);
                    p.put("type", pd.type);
                    p.put("required", pd.required);
                    p.put("default", pd.defaultValue);
                    params.add(p);
                }
            }
            qinfo.put("params", params);
            
            queries.add(qinfo);
        }

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("success", true);
        response.put("count", queries.size());
        response.put("queries", queries);

        try {
            exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "application/json");
            exchange.setStatusCode(200);
            exchange.getResponseSender().send(mapper.writeValueAsString(response));
        } catch (Exception e) {
            exchange.setStatusCode(500);
            exchange.getResponseSender().send("{\"error\":\"Failed\"}");
        }
    }
}
