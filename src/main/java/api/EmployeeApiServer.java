package api;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import dao.EmployeeDAO;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class EmployeeApiServer {

    private static final Gson gson = new Gson();

    public static void main(String[] args) throws IOException {

        HttpServer server = HttpServer.create(
                new InetSocketAddress(8080),
                0
        );

        server.createContext(
                "/api/employees",
                EmployeeApiServer::handleEmployees
        );

        server.createContext(
                "/api/employees/count",
                EmployeeApiServer::handleEmployeeCount
        );

        server.setExecutor(null);

        server.start();

        System.out.println(
                "EITASM API running on http://localhost:8080"
        );
    }


    private static void handleEmployees(HttpExchange exchange)
            throws IOException {

        addCorsHeaders(exchange);

        if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(204, -1);
            return;
        }

        if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            sendJson(
                    exchange,
                    405,
                    Map.of("error", "Method not allowed")
            );
            return;
        }

        try {

            EmployeeDAO employeeDAO = new EmployeeDAO();

            String json = gson.toJson(
                    employeeDAO.getEmployeesForApi()
            );

            sendRawJson(
                    exchange,
                    200,
                    json
            );

        } catch (SQLException ex) {

            ex.printStackTrace();

            sendJson(
                    exchange,
                    500,
                    Map.of(
                            "error",
                            "Database error",
                            "message",
                            ex.getMessage()
                    )
            );
        }
    }


    private static void handleEmployeeCount(HttpExchange exchange)
            throws IOException {

        addCorsHeaders(exchange);

        if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(204, -1);
            return;
        }

        if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            sendJson(
                    exchange,
                    405,
                    Map.of("error", "Method not allowed")
            );
            return;
        }

        try {

            EmployeeDAO employeeDAO = new EmployeeDAO();

            int count = employeeDAO.getEmployeeCount();

            Map<String, Object> response = new HashMap<>();

            response.put(
                    "count",
                    count
            );

            sendJson(
                    exchange,
                    200,
                    response
            );

        } catch (SQLException ex) {

            ex.printStackTrace();

            sendJson(
                    exchange,
                    500,
                    Map.of(
                            "error",
                            "Database error",
                            "message",
                            ex.getMessage()
                    )
            );
        }
    }


    private static void sendJson(
            HttpExchange exchange,
            int statusCode,
            Object data
    ) throws IOException {

        String json = gson.toJson(data);

        sendRawJson(
                exchange,
                statusCode,
                json
        );
    }


    private static void sendRawJson(
            HttpExchange exchange,
            int statusCode,
            String json
    ) throws IOException {

        byte[] bytes = json.getBytes(
                StandardCharsets.UTF_8
        );

        exchange.getResponseHeaders().set(
                "Content-Type",
                "application/json; charset=UTF-8"
        );

        addCorsHeaders(exchange);

        exchange.sendResponseHeaders(
                statusCode,
                bytes.length
        );

        try (OutputStream outputStream =
                     exchange.getResponseBody()) {

            outputStream.write(bytes);
        }
    }


    private static void addCorsHeaders(
            HttpExchange exchange
    ) {

        exchange.getResponseHeaders().set(
                "Access-Control-Allow-Origin",
                "*"
        );

        exchange.getResponseHeaders().set(
                "Access-Control-Allow-Methods",
                "GET, OPTIONS"
        );

        exchange.getResponseHeaders().set(
                "Access-Control-Allow-Headers",
                "Content-Type"
        );
    }
}