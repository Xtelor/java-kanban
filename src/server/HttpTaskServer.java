package server;

import com.google.gson.JsonParseException;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import exceptions.NotFoundException;
import exceptions.TasksOverlapsException;
import manager.*;
import tasks.Epic;
import tasks.Subtask;
import tasks.Task;
import handlers.BaseHttpHandler;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import tasks.TaskStatus;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class HttpTaskServer {
    private HttpServer httpServer;
    private static final int PORT = 8080;
    private final TaskManager taskManager;
    private final Gson gson;

    public HttpTaskServer() {
        this.taskManager = Managers.getDefault();
        this.gson = new GsonBuilder()
                .registerTypeAdapter(TaskStatus.class, new TaskStatusAdapter())
                .registerTypeAdapter(Duration.class, new DurationTypeAdapter())
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                .create();
    }

    public HttpTaskServer(TaskManager taskManager) {
        this.taskManager = taskManager;
        this.gson = new GsonBuilder()
                .registerTypeAdapter(TaskStatus.class, new TaskStatusAdapter())
                .registerTypeAdapter(Duration.class, new DurationTypeAdapter())
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                .create();
    }

    public static void main(String[] args) {
        HttpTaskServer server = new HttpTaskServer();
        server.start();
    }

    public void start() {
        try {
            httpServer = HttpServer.create(new InetSocketAddress(PORT), 0);

            httpServer.createContext("/tasks", new TaskHandler());
            httpServer.createContext("/subtasks", new SubtaskHandler());
            httpServer.createContext("/epics", new EpicHandler());
            httpServer.createContext("/history", new HistoryHandler());
            httpServer.createContext("/prioritized", new PrioritizedHandler());

            System.out.println("HTTP-сервер запущен на порту " + PORT + ".");
            httpServer.start();
        } catch (IOException e) {
            throw new RuntimeException("Ошибка при запуске сервера: " + e.getMessage(), e);
        }
    }

    public void stop() {
        if (httpServer != null) {
            System.out.println("Работа сервера остановлена.");
            httpServer.stop(0);
        }
    }

    public Gson getGson() {
        return this.gson;
    }

    class TaskHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange httpExchange) throws IOException {
            String method = httpExchange.getRequestMethod();
            String path = httpExchange.getRequestURI().getPath();

            // getTasks()
            if (method.equals("GET") && path.equals("/tasks")) {
                List<Task> tasks = taskManager.getTasks();
                sendJsonResponse(httpExchange, tasks);

            //  getTaskById()
            } else if (method.equals("GET") && path.startsWith("/tasks/")) {

                try {
                    Task task = taskManager.getTaskById(getIdFromPath(path));
                    sendJsonResponse(httpExchange, task);
                } catch (NotFoundException e) {
                    BaseHttpHandler.sendNotFound(httpExchange, e.getMessage());
                }

            // createNewTask()
            } else if (method.equals("POST") && path.equals("/tasks")) {

                try {
                    Task task = parseJsonRequest(httpExchange, Task.class);
                    taskManager.createNewTask(task);
                    BaseHttpHandler.sendCreated(httpExchange);
                } catch (TasksOverlapsException e) {
                    BaseHttpHandler.sendHasOverlaps(httpExchange, e.getMessage());
                } catch (Exception e) {
                    BaseHttpHandler.sendBadRequest(httpExchange,
                            "Ошибка при обработке запроса: " + e.getMessage());
                }

            // updateTask()
            } else if (method.equals("POST") && path.startsWith("/tasks/")) {

                try {
                    Task task = parseJsonRequest(httpExchange, Task.class);
                    taskManager.updateTask(task);
                    BaseHttpHandler.sendCreated(httpExchange);
                } catch (NotFoundException e) {
                    BaseHttpHandler.sendNotFound(httpExchange, e.getMessage());
                } catch (TasksOverlapsException e) {
                    BaseHttpHandler.sendHasOverlaps(httpExchange, e.getMessage());
                } catch (Exception e) {
                    BaseHttpHandler.sendBadRequest(httpExchange,
                            "Ошибка при обработке запроса: " + e.getMessage());
                }

                // deleteTaskById()
            } else if (method.equals("DELETE") && path.matches("/tasks/\\d+")) {
                try {
                    Task task = taskManager.deleteTaskById(getIdFromPath(path));
                    sendJsonResponse(httpExchange, task);
                } catch (NotFoundException e) {
                    BaseHttpHandler.sendNotFound(httpExchange, e.getMessage());
                }

            // deleteAllTasks()
            } else if (method.equals("DELETE") && path.equals("/tasks")) {

                taskManager.deleteAllTasks();
                BaseHttpHandler.sendCreated(httpExchange);

            }
        }
    }

    class SubtaskHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange httpExchange) throws IOException {
            String method = httpExchange.getRequestMethod();
            String path = httpExchange.getRequestURI().getPath();

            // getSubtasks()
            if (method.equals("GET") && path.equals("/subtasks")) {

                List<Subtask> subtasks = taskManager.getSubtasks();
                sendJsonResponse(httpExchange, subtasks);

            //  getSubtaskById()
            } else if (method.equals("GET") && path.startsWith("/subtasks/")) {

                try {
                    Subtask subtask = taskManager.getSubtaskById(getIdFromPath(path));
                    sendJsonResponse(httpExchange, subtask);
                } catch (NotFoundException e) {
                    BaseHttpHandler.sendNotFound(httpExchange, e.getMessage());
                }

            // createNewSubtask()
            } else if (method.equals("POST") && path.equals("/subtasks")) {

                try {
                    Subtask subtask = parseJsonRequest(httpExchange, Subtask.class);

                    List<Epic> epics = taskManager.getEpics();
                    int epicId = subtask.getEpicIdentifier();

                    boolean epicExists = epicId > 0 && epics != null
                            && epics.stream().anyMatch(epic -> epic.getTaskId() == epicId);

                    if (!epicExists) {
                        if (epics == null || epics.isEmpty()) {
                            BaseHttpHandler.sendNotFound(httpExchange,
                                    "Не удалось создать подзадачу: эпик не найден.");
                            return;
                        }

                        Optional<Epic> latestEpic = epics.stream()
                                .max(Comparator.comparing(Epic::getTaskId));
                        subtask.setEpicIdentifier(latestEpic.get().getTaskId());
                    }

                    taskManager.createNewSubtask(taskManager.getEpicById(subtask.getEpicIdentifier()), subtask);
                    BaseHttpHandler.sendCreated(httpExchange);

                } catch (TasksOverlapsException e) {
                    BaseHttpHandler.sendHasOverlaps(httpExchange, e.getMessage());
                } catch (NotFoundException e) {
                    BaseHttpHandler.sendNotFound(httpExchange, e.getMessage());
                }

                // updateSubtask()
            } else if (method.equals("POST") && path.startsWith("/subtasks/")) {

                try {
                    Subtask subtask = parseJsonRequest(httpExchange, Subtask.class);
                    taskManager.updateSubtask(subtask);
                    BaseHttpHandler.sendCreated(httpExchange);
                } catch (NotFoundException e) {
                    BaseHttpHandler.sendNotFound(httpExchange, e.getMessage());
                } catch (TasksOverlapsException e) {
                    BaseHttpHandler.sendHasOverlaps(httpExchange, e.getMessage());
                }

            // deleteSubtaskById()
            } else if (method.equals("DELETE") && path.matches("/subtasks/\\d+")) {
                try {
                    Subtask deleted = taskManager.deleteSubtaskById(getIdFromPath(path));
                    sendJsonResponse(httpExchange, deleted);
                } catch (NotFoundException e) {
                    BaseHttpHandler.sendNotFound(httpExchange, e.getMessage());
                }

             // deleteAllSubtasks()
            } else if (method.equals("DELETE") && path.equals("/subtasks")) {
                taskManager.deleteAllSubtasks();
                BaseHttpHandler.sendCreated(httpExchange);
            }
        }
    }

    class EpicHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange httpExchange) throws IOException {
            String method = httpExchange.getRequestMethod();
            String path = httpExchange.getRequestURI().getPath();

            // getEpics()
            if (method.equals("GET") && path.equals("/epics")) {
                List<Epic> epics = taskManager.getEpics();
                sendJsonResponse(httpExchange, epics);

                // getEpicSubtasks()
            } else if (method.equals("GET") && path.matches("^/epics/\\d+/subtasks$")) {
                try {
                    List<Subtask> subtasks =
                            taskManager.getEpicSubtasks(taskManager.getEpicById(getIdFromMiddlePath(path)));
                    sendJsonResponse(httpExchange, subtasks);
                } catch (NotFoundException e) {
                    BaseHttpHandler.sendNotFound(httpExchange, e.getMessage());
                } catch (Exception e) {
                    BaseHttpHandler.sendBadRequest(httpExchange,
                            "Ошибка при обработке запроса: " + e.getMessage());
                }

            // getEpicById()
            } else if (method.equals("GET") && path.startsWith("/epics/")) {

                try {
                    Epic epic = taskManager.getEpicById(getIdFromPath(path));
                    sendJsonResponse(httpExchange, epic);
                } catch (NotFoundException e) {
                    BaseHttpHandler.sendNotFound(httpExchange, e.getMessage());
                }

            // createNewEpic()
            } else if (method.equals("POST") && path.equals("/epics")) {

                try {
                    Epic epic = parseJsonRequest(httpExchange, Epic.class);
                    taskManager.createNewEpic(epic);
                    BaseHttpHandler.sendCreated(httpExchange);
                } catch (TasksOverlapsException e) {
                    BaseHttpHandler.sendHasOverlaps(httpExchange, e.getMessage());
                } catch (Exception e) {
                    BaseHttpHandler.sendBadRequest(httpExchange,
                            "Ошибка при обработке запроса: " + e.getMessage());
                }

            // deleteEpicById()
            } else if (method.equals("DELETE") && path.matches("/epics/\\d+")) {
                try {
                    Epic epic = taskManager.deleteEpicById(getIdFromPath(path));
                    sendJsonResponse(httpExchange, epic);
                } catch (NotFoundException e) {
                    BaseHttpHandler.sendNotFound(httpExchange, e.getMessage());
                }

            // deleteAllEpics()
            } else if (method.equals("DELETE") && path.equals("/epics")) {

                taskManager.deleteAllEpics();
                BaseHttpHandler.sendCreated(httpExchange);

            }
        }
    }

    class HistoryHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange httpExchange) throws IOException {
            String method = httpExchange.getRequestMethod();
            String path = httpExchange.getRequestURI().getPath();

            // getHistory()
            if (method.equals("GET") && path.equals("/history")) {
                List<Task> history = taskManager.getHistory();
                sendJsonResponse(httpExchange, history);
            }
        }
    }

    class PrioritizedHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange httpExchange) throws IOException {
            String method = httpExchange.getRequestMethod();
            String path = httpExchange.getRequestURI().getPath();

            // getPrioritizedTasks()
            if (method.equals("GET") && path.equals("/prioritized")) {
                List<Task> prioritizedTasks = taskManager.getPrioritizedTasks();
                sendJsonResponse(httpExchange, prioritizedTasks);
            }
        }
    }

    private static class LocalDateTimeAdapter extends TypeAdapter<LocalDateTime> {
        private static final DateTimeFormatter dtf = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

        @Override
        public void write(JsonWriter out, LocalDateTime value) throws IOException {
            out.value(value.format(dtf));
        }

        @Override
        public LocalDateTime read(JsonReader in) throws IOException {
            return LocalDateTime.parse(in.nextString(), dtf);
        }
    }

    private static class DurationTypeAdapter extends TypeAdapter<Duration> {
        @Override
        public void write(JsonWriter out, Duration value) throws IOException {
            if (value == null) {
                out.nullValue();
                return;
            }
            out.value(value.toMinutes());
        }

        @Override
        public Duration read(JsonReader in) throws IOException {
            if (in.peek() == JsonToken.NULL) {
                in.nextNull();
                return null;
            }
            return Duration.ofMinutes(in.nextLong());
        }
    }

    private static class TaskStatusAdapter extends TypeAdapter<TaskStatus> {
        @Override
        public void write(JsonWriter out, TaskStatus value) throws IOException {
            out.value(value.name());
        }

        @Override
        public TaskStatus read(JsonReader in) throws IOException {
            String value = in.nextString();
            try {
                return TaskStatus.valueOf(value);
            } catch (IllegalArgumentException e) {
                throw new JsonParseException("Неизвестный статус задачи: " + value);
            }
        }
    }

    // Метод для получения ID из конца пути
    private static int getIdFromPath(String path) throws IOException {
        String[] result = path.split("/");
        try {
            return Integer.parseInt(result[result.length - 1]);
        } catch (NumberFormatException e) {
            throw new IOException("Передан неправильный ID: " + result[result.length - 1]);
        }
    }

    // Метод для получения ID из середины пути
    private static int getIdFromMiddlePath(String path) throws IOException {
        String[] result = path.split("/");
        try {
            return Integer.parseInt(result[2]);
        } catch (NumberFormatException e) {
            throw new IOException("Передан неправильный ID: " + result[2]);
        }
    }

    private <T> T parseJsonRequest(HttpExchange exchange, Class<T> taskClass) throws IOException {
        try (InputStreamReader reader = new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8)) {
            return gson.fromJson(reader, taskClass);
        }
    }

    private void sendJsonResponse(HttpExchange exchange, Object data) throws IOException {
        String response = gson.toJson(data);
        BaseHttpHandler.sendSuccess(exchange, response);
    }
}
