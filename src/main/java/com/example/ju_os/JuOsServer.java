package com.example.ju_os;

import io.undertow.Undertow;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;
import com.example.ju_os.config.ConfigLoader;
import com.example.ju_os.handler.*;

public class JuOsServer {
    public static void main(String[] args) {
        System.out.println("=========================================");
        System.out.println("  ju-os - Undertow Oracle HTTP Server");
        System.out.println("=========================================");
        ConfigLoader cfg = ConfigLoader.getInstance();
        int port = cfg.getServerPort();
        Undertow server = Undertow.builder()
            .addHttpListener(port, cfg.getServerHost())
            .setHandler(JuOsServer::routeRequest).build();
        server.start();
        System.out.println("Started on port " + port);
    }
    private static void routeRequest(HttpServerExchange ex) {
        String path = ex.getRequestPath();
        String prefix = ConfigLoader.getInstance().getApiPrefix();
        try {
            if (path.equals("/")) {
                ex.getResponseHeaders().put(Headers.CONTENT_TYPE, "application/json");
                ex.getResponseSender().send("{\"name\":\"ju-os\"}");
            } else if (path.equals(prefix + "/health")) {
                new HealthHandler().handleRequest(ex);
            } else if (path.startsWith(prefix)) {
                routeApi(path.substring(prefix.length()), ex);
            } else {
                ex.setStatusCode(404);
                ex.getResponseSender().send("{\"error\":\"Not Found\"}");
            }
        } catch (Exception e) {
            ex.setStatusCode(500);
            ex.getResponseSender().send("{\"error\":\"" + e.getMessage() + "\"}");
        }
    }
    private static void routeApi(String p, HttpServerExchange ex) {
        String m = ex.getRequestMethod().toString();
        if (p.equals("/tables") || p.equals("/tables/")) new TableListHandler().handleRequest(ex);
        else if (p.equals("/views") || p.equals("/views/")) new TableListHandler().handleRequest(ex);
        else if (p.equals("/all") || p.equals("/all/")) new TableListHandler().handleRequest(ex);
        else if (p.startsWith("/table/") && p.endsWith("/data")) new TableDataHandler().handleRequest(ex);
        else if (p.startsWith("/table/") && p.endsWith("/csv")) new TableCsvHandler().handleRequest(ex);
        else if (p.startsWith("/table/") && p.contains("/row/")) new RowHandler().handleRequest(ex);
        else if (p.startsWith("/table/") && p.contains("/update/") && (m.equals("PUT") || m.equals("POST"))) new UpdateHandler().handleRequest(ex);
        else if (p.startsWith("/table/") && p.contains("/delete/") && m.equals("DELETE")) new DeleteHandler().handleRequest(ex);
        else { ex.setStatusCode(404); ex.getResponseSender().send("{\"error\":\"Not Found\"}"); }
    }
}
