package com.example.ju_os.handler;

import com.example.ju_os.config.QueryConfig;
import com.example.ju_os.db.QueryExecutor;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;
import java.util.*;

public class QueryHandlers {
    private static final ObjectMapper mapper = new ObjectMapper();
    private static final QueryExecutor executor = new QueryExecutor();

    // Base handler class for query endpoints
    public static abstract class AbstractQueryHandler implements HttpHandler {
        protected abstract String getQueryName();
        protected abstract String getMethod();
        protected abstract Map<String, Object> extractParams(HttpServerExchange exchange);

        public void handleRequest(HttpServerExchange exchange) throws Exception {
            String method = exchange.getRequestMethod().toString();
            if (!method.equalsIgnoreCase(getMethod())) {
                sendError(exchange, "Method " + method + " not allowed", 405);
                return;
            }

            Map<String, Object> params = extractParams(exchange);

            try {
                QueryConfig.QueryDefinition qd = QueryConfig.getInstance().getQuery(getQueryName());
                if (qd == null) {
                    sendError(exchange, "Query not found: " + getQueryName(), 404);
                    return;
                }

                Map<String, Object> response = new LinkedHashMap<>();
                response.put("query", getQueryName());

                if ("select".equalsIgnoreCase(qd.type)) {
                    List<Map<String, Object>> data = executor.executeSelect(getQueryName(), params);
                    response.put("success", true);
                    response.put("data", data);
                    response.put("rowCount", data.size());
                    sendJson(exchange, response, 200);
                } else {
                    int affected = executor.executeUpdate(getQueryName(), params);
                    response.put("success", true);
                    response.put("affectedRows", affected);
                    sendJson(exchange, response, 200);
                }
            } catch (IllegalArgumentException e) {
                sendError(exchange, e.getMessage(), 400);
            } catch (Exception e) {
                sendError(exchange, "Execution failed: " + e.getMessage(), 500);
            }
        }

        protected void sendJson(HttpServerExchange exchange, Map<String, Object> data, int status) {
            try {
                exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "application/json");
                exchange.setStatusCode(status);
                exchange.getResponseSender().send(mapper.writeValueAsString(data));
            } catch (Exception e) {
                exchange.setStatusCode(500);
                exchange.getResponseSender().send("{\"error\":\"Failed\"}");
            }
        }

        protected void sendError(HttpServerExchange exchange, String message, int status) {
            sendJson(exchange, Map.of("success", false, "error", message), status);
        }
    }

    // GET /api/v1/employees - List employees with filters
    public static class GetEmployeesHandler extends AbstractQueryHandler {
        protected String getQueryName() { return "getEmployees"; }
        protected String getMethod() { return "GET"; }
        protected Map<String, Object> extractParams(HttpServerExchange exchange) {
            Map<String, Object> params = new LinkedHashMap<>();
            Map<String, Deque<String>> qp = exchange.getQueryParameters();
            if (qp.containsKey("departmentId")) params.put("departmentId", qp.get("departmentId").getFirst());
            if (qp.containsKey("managerId")) params.put("managerId", qp.get("managerId").getFirst());
            if (qp.containsKey("minSalary")) params.put("minSalary", qp.get("minSalary").getFirst());
            if (qp.containsKey("maxSalary")) params.put("maxSalary", qp.get("maxSalary").getFirst());
            if (qp.containsKey("jobId")) params.put("jobId", qp.get("jobId").getFirst());
            if (qp.containsKey("offset")) params.put("offset", qp.get("offset").getFirst());
            if (qp.containsKey("limit")) params.put("limit", qp.get("limit").getFirst());
            return params;
        }
    }

    // GET /api/v1/employees/{id} - Get employee by ID
    public static class GetEmployeeByIdHandler extends AbstractQueryHandler {
        protected String getQueryName() { return "getEmployeeById"; }
        protected String getMethod() { return "GET"; }
        protected Map<String, Object> extractParams(HttpServerExchange exchange) {
            String path = exchange.getRequestPath();
            String id = path.substring(path.lastIndexOf("/") + 1);
            return Map.of("employeeId", id);
        }
    }

    // PUT /api/v1/employees/{id}/salary - Update salary
    public static class UpdateSalaryHandler extends AbstractQueryHandler {
        protected String getQueryName() { return "updateEmployeeSalary"; }
        protected String getMethod() { return "PUT"; }
        protected Map<String, Object> extractParams(HttpServerExchange exchange) {
            String path = exchange.getRequestPath();
            String id = path.split("/")[4];
            Map<String, Object> params = new LinkedHashMap<>();
            params.put("employeeId", id);
            try {
                String body = exchange.getInputStream().read().toString();
                if (body != null && !body.isEmpty()) {
                    Map<String, Object> bodyParams = mapper.readValue(body, Map.class);
                    params.putAll(bodyParams);
                }
            } catch (Exception e) {}
            return params;
        }
    }

    // POST /api/v1/employees - Insert new employee
    public static class InsertEmployeeHandler extends AbstractQueryHandler {
        protected String getQueryName() { return "insertEmployee"; }
        protected String getMethod() { return "POST"; }
        protected Map<String, Object> extractParams(HttpServerExchange exchange) {
            try {
                String body = exchange.getInputStream().read().toString();
                if (body != null && !body.isEmpty()) {
                    return mapper.readValue(body, Map.class);
                }
            } catch (Exception e) {}
            return new LinkedHashMap<>();
        }
    }

    // DELETE /api/v1/employees/{id} - Delete employee
    public static class DeleteEmployeeHandler extends AbstractQueryHandler {
        protected String getQueryName() { return "deleteEmployee"; }
        protected String getMethod() { return "DELETE"; }
        protected Map<String, Object> extractParams(HttpServerExchange exchange) {
            String path = exchange.getRequestPath();
            String id = path.substring(path.lastIndexOf("/") + 1);
            return Map.of("employeeId", id);
        }
    }

    // GET /api/v1/departments - List departments
    public static class GetDepartmentsHandler extends AbstractQueryHandler {
        protected String getQueryName() { return "getDepartments"; }
        protected String getMethod() { return "GET"; }
        protected Map<String, Object> extractParams(HttpServerExchange exchange) {
            Map<String, Object> params = new LinkedHashMap<>();
            Map<String, Deque<String>> qp = exchange.getQueryParameters();
            if (qp.containsKey("offset")) params.put("offset", qp.get("offset").getFirst());
            if (qp.containsKey("limit")) params.put("limit", qp.get("limit").getFirst());
            return params;
        }
    }

    // GET /api/v1/jobs - List jobs
    public static class GetJobsHandler extends AbstractQueryHandler {
        protected String getQueryName() { return "getJobs"; }
        protected String getMethod() { return "GET"; }
        protec
