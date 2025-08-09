package handlers;

import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class BaseHttpHandler {

    public static void sendSuccess(HttpExchange exchange, String text) throws IOException {
        sendText(exchange, text, 200);
    }

    public static void sendCreated(HttpExchange exchange) throws IOException {
        sendText(exchange,"", 201);
    }

    public static void sendNotFound(HttpExchange exchange, String errorMessage) throws IOException {
        String jsonError = String.format("{\"error\": \"%s\"}", errorMessage);
        sendText(exchange, jsonError, 404);
    }

    public static void sendHasOverlaps(HttpExchange exchange, String errorMessage) throws IOException {
        String jsonError = String.format("{\"error\": \"%s\"}", errorMessage);
        sendText(exchange, jsonError, 406);
    }

    public static void sendBadRequest(HttpExchange exchange, String errorMessage) throws IOException {
        String jsonError = String.format("{\"error\": \"%s\"}", errorMessage);
        sendText(exchange, jsonError, 400);
    }

    protected static void sendText(HttpExchange exchange, String text, int statusCode) throws IOException {
        byte[] response = text.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Content-Type", "application/json;charset=utf-8");
        exchange.sendResponseHeaders(statusCode, response.length);
        exchange.getResponseBody().write(response);
        exchange.close();
    }

}