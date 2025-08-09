package endpoints;

import com.google.gson.*;
import manager.*;
import server.HttpTaskServer;
import tasks.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.*;

public class HistoryEndpointTest {
    private final TaskManager manager = new InMemoryTaskManager();
    private final HttpTaskServer taskServer = new HttpTaskServer(manager);
    private final Gson gson = taskServer.getGson();
    private ResponseManager responseManager;
    private final LocalDateTime date = LocalDateTime.of(2025, 5, 12, 14, 54);
    private final long duration = 5;

    private HistoryEndpointTest() {

    }

    @BeforeEach
    void beforeEach() {
        manager.deleteAllEpics();
        manager.deleteAllSubtasks();
        manager.deleteAllTasks();
        taskServer.start();
        responseManager = new ResponseManager();
    }

    @AfterEach
    void afterEach() {
        taskServer.stop();
    }

    @Test // Проверка получения пустого списка истории
    void testGetEmptyHistory() throws IOException, InterruptedException {
        // Запрос на получение списка истории
        HttpResponse<String> response = responseManager.sendGetResponse("/history");

        assertEquals(200, response.statusCode(), "Некорректный код статуса");

        List<Task> history = gson.fromJson(response.body(), new TypeTokens.TasksListTypeToken().getType());
        assertTrue(history.isEmpty(), "Список должен быть пуст");
    }

    @Test // Проверка получения корректного списка истории
    void testGetCorrectHistoryList() throws IOException, InterruptedException {
        Task task = new Task("Тест 1", "Проверка создания", TaskStatus.NEW,
                duration, date);
        Epic epic = new Epic("Эпик 1", "Создание");
        Subtask subtask = new Subtask(1, "Тест", "Проверка обновления",
                TaskStatus.DONE, duration, date.minusDays(1));

        // Запрос на создание задачи
        responseManager.sendPostResponse("/tasks", task);

        // Запрос на создание эпика
        responseManager.sendPostResponse("/epics", epic);

        // Запрос на создание подзадачи
        responseManager.sendPostResponse("/subtasks", subtask);

        // Запрос на получение задачи по ID
        HttpResponse<String> taskResponse = responseManager.sendGetResponse("/tasks/1");

        // Запрос на получение эпика по ID
        HttpResponse<String> epicResponse = responseManager.sendGetResponse("/epics/2");

        // Запрос на получение подзадачи по ID
        HttpResponse<String> subtaskResponse = responseManager.sendGetResponse("/subtasks/3");

        // Запрос на получение списка истории
        HttpResponse<String> response = responseManager.sendGetResponse("/history");

        assertEquals(200, response.statusCode(), "Некорректный код статуса");

        Task receivedTask = gson.fromJson(taskResponse.body(), Task.class);
        Task receivedEpic = gson.fromJson(epicResponse.body(), Task.class);
        Task receivedSubtask = gson.fromJson(subtaskResponse.body(), Task.class);
        List<Task> history = gson.fromJson(response.body(), new TypeTokens.TasksListTypeToken().getType());

        assertFalse(history.isEmpty(), "Список не должен быть пуст");
        assertEquals(3, history.size(), "В списке должно быть 3 задачи");
        assertTrue(history.contains(receivedTask),"В списке отсутствует задача");
        assertTrue(history.contains(receivedEpic),"В списке отсутствует эпик");
        assertTrue(history.contains(receivedSubtask),"В списке отсутствует подзадача");

        assertEquals(receivedTask, history.getFirst(), "Первой должна быть задача");
        assertEquals(receivedSubtask, history.getLast(), "Последней должна быть подзадача");
    }

    @Test // Проверка получения корректного списка истории при удалении задачи
    void testGetCorrectHistoryListAfterTaskRemoval() throws IOException, InterruptedException {
        Task task = new Task("Тест 1", "Проверка создания", TaskStatus.NEW,
                duration, date);
        Epic epic = new Epic("Эпик 1", "Создание");
        Subtask subtask = new Subtask(1, "Тест", "Проверка обновления",
                TaskStatus.DONE, duration, date.minusDays(1));

        // Запрос на создание задачи
        responseManager.sendPostResponse("/tasks", task);

        // Запрос на создание эпика
        responseManager.sendPostResponse("/epics", epic);

        // Запрос на создание подзадачи
        responseManager.sendPostResponse("/subtasks", subtask);

        // Запрос на получение задачи по ID
        responseManager.sendGetResponse("/tasks/1");

        // Запрос на получение эпика по ID
        HttpResponse<String> epicResponse = responseManager.sendGetResponse("/epics/2");

        // Запрос на получение подзадачи по ID
        HttpResponse<String> subtaskResponse = responseManager.sendGetResponse("/subtasks/3");

        // Запрос на удаление задачи по ID
        responseManager.sendDeleteResponse("/tasks/1");

        // Запрос на получение списка истории
        HttpResponse<String> response = responseManager.sendGetResponse("/history");

        assertEquals(200, response.statusCode(), "Некорректный код статуса");

        Task receivedEpic = gson.fromJson(epicResponse.body(), Task.class);
        Task receivedSubtask = gson.fromJson(subtaskResponse.body(), Task.class);
        List<Task> history = gson.fromJson(response.body(), new TypeTokens.TasksListTypeToken().getType());

        assertFalse(history.isEmpty(), "Список не должен быть пуст");
        assertEquals(2, history.size(), "В списке должно быть 2 задачи");

        assertTrue(history.contains(receivedEpic),"В списке отсутствует эпик");
        assertTrue(history.contains(receivedSubtask),"В списке отсутствует подзадача");

        assertEquals(receivedEpic, history.getFirst(), "Первым должен быть эпик");
        assertEquals(receivedSubtask, history.getLast(), "Последней должна быть подзадача");
    }

    @Test // Проверка получения корректного списка истории при удалении эпика
    void testGetCorrectHistoryListAfterEpicRemoval() throws IOException, InterruptedException {
        Task task = new Task("Тест 1", "Проверка создания", TaskStatus.NEW,
                duration, date);
        Epic epic = new Epic("Эпик 1", "Создание");
        Subtask subtask = new Subtask(1, "Тест", "Проверка обновления",
                TaskStatus.DONE, duration, date.minusDays(1));

        // Запрос на создание задачи
        responseManager.sendPostResponse("/tasks", task);

        // Запрос на создание эпика
        responseManager.sendPostResponse("/epics", epic);

        // Запрос на создание подзадачи
        responseManager.sendPostResponse("/subtasks", subtask);

        // Запрос на получение задачи по ID
        HttpResponse<String> taskResponse =  responseManager.sendGetResponse("/tasks/1");

        // Запрос на получение эпика по ID
        responseManager.sendGetResponse("/epics/2");

        // Запрос на получение подзадачи по ID
        HttpResponse<String> subtaskResponse = responseManager.sendGetResponse("/subtasks/3");

        // Запрос на удаление эпика по ID
        responseManager.sendDeleteResponse("/epics/2");

        // Запрос на получение списка истории
        HttpResponse<String> response = responseManager.sendGetResponse("/history");

        assertEquals(200, response.statusCode(), "Некорректный код статуса");


        Task receivedTask = gson.fromJson(taskResponse.body(), Task.class);
        Task receivedSubtask = gson.fromJson(subtaskResponse.body(), Task.class);
        List<Task> history = gson.fromJson(response.body(), new TypeTokens.TasksListTypeToken().getType());

        assertFalse(history.isEmpty(), "Список не должен быть пуст");
        assertEquals(1, history.size(), "В списке должно быть 2 задачи");
        assertTrue(history.contains(receivedTask),"В списке отсутствует задача");
        assertFalse(history.contains(receivedSubtask),"В списке присутствует подзадача");

        assertEquals(receivedTask, history.getFirst(), "Первой должна быть задача");
        assertEquals(receivedTask, history.getLast(), "Последней должна быть задача");
    }

    @Test // Проверка получения корректного списка истории при удалении подзадачи
    void testGetCorrectHistoryListAfterSubtaskRemoval() throws IOException, InterruptedException {
        Task task = new Task("Тест 1", "Проверка создания", TaskStatus.NEW,
                duration, date);
        Epic epic = new Epic("Эпик 1", "Создание");
        Subtask subtask = new Subtask(1, "Тест", "Проверка обновления",
                TaskStatus.DONE, duration, date.minusDays(1));

        // Запрос на создание задачи
        responseManager.sendPostResponse("/tasks", task);

        // Запрос на создание эпика
        responseManager.sendPostResponse("/epics", epic);

        // Запрос на создание подзадачи
        responseManager.sendPostResponse("/subtasks", subtask);

        // Запрос на получение задачи по ID
        HttpResponse<String> taskResponse = responseManager.sendGetResponse("/tasks/1");

        // Запрос на получение эпика по ID
        HttpResponse<String> epicResponse = responseManager.sendGetResponse("/epics/2");

        // Запрос на получение подзадачи по ID
        responseManager.sendGetResponse("/subtasks/3");

        // Запрос на удаление подзадачи по ID
        responseManager.sendDeleteResponse("/subtasks/3");

        // Запрос на получение списка истории
        HttpResponse<String> response = responseManager.sendGetResponse("/history");

        assertEquals(200, response.statusCode(), "Некорректный код статуса");

        Task receivedTask = gson.fromJson(taskResponse.body(), Task.class);
        Task receivedEpic = gson.fromJson(epicResponse.body(), Task.class);
        List<Task> history = gson.fromJson(response.body(), new TypeTokens.TasksListTypeToken().getType());

        assertFalse(history.isEmpty(), "Список не должен быть пуст");
        assertEquals(2, history.size(), "В списке должно быть 3 задачи");
        assertTrue(history.contains(receivedTask),"В списке отсутствует задача");
        assertTrue(history.contains(receivedEpic),"В списке отсутствует эпик");

        assertEquals(receivedTask, history.getFirst(), "Первой должна быть задача");
        assertEquals(receivedEpic, history.getLast(), "Последним должен быть эпик");
    }

    @Test // Проверка получения корректного списка истории при удалении задач
    void testGetCorrectHistoryListAfterTasksRemoval() throws IOException, InterruptedException {
        Task task = new Task("Тест 1", "Проверка создания", TaskStatus.NEW,
                duration, date);
        Epic epic = new Epic("Эпик 1", "Создание");
        Subtask subtask = new Subtask(1, "Тест", "Проверка обновления",
                TaskStatus.DONE, duration, date.minusDays(1));

        // Запрос на создание задачи
        responseManager.sendPostResponse("/tasks", task);

        // Запрос на создание эпика
        responseManager.sendPostResponse("/epics", epic);

        // Запрос на создание подзадачи
        responseManager.sendPostResponse("/subtasks", subtask);

        // Запрос на получение задачи по ID
        responseManager.sendGetResponse("/tasks/1");

        // Запрос на получение эпика по ID
        HttpResponse<String> epicResponse = responseManager.sendGetResponse("/epics/2");

        // Запрос на получение подзадачи по ID
        HttpResponse<String> subtaskResponse = responseManager.sendGetResponse("/subtasks/3");

        // Запрос на удаление задач
        responseManager.sendDeleteResponse("/tasks");

        // Запрос на получение списка истории
        HttpResponse<String> response = responseManager.sendGetResponse("/history");

        assertEquals(200, response.statusCode(), "Некорректный код статуса");

        Task receivedEpic = gson.fromJson(epicResponse.body(), Task.class);
        Task receivedSubtask = gson.fromJson(subtaskResponse.body(), Task.class);
        List<Task> history = gson.fromJson(response.body(), new TypeTokens.TasksListTypeToken().getType());

        assertFalse(history.isEmpty(), "Список не должен быть пуст");
        assertEquals(2, history.size(), "В списке должно быть 2 задачи");

        assertTrue(history.contains(receivedEpic),"В списке отсутствует эпик");
        assertTrue(history.contains(receivedSubtask),"В списке отсутствует подзадача");

        assertEquals(receivedEpic, history.getFirst(), "Первым должен быть эпик");
        assertEquals(receivedSubtask, history.getLast(), "Последней должна быть подзадача");
    }

    @Test // Проверка получения корректного списка истории при удалении эпиков
    void testGetCorrectHistoryListAfterEpicsRemoval() throws IOException, InterruptedException {
        Task task = new Task("Тест 1", "Проверка создания", TaskStatus.NEW,
                duration, date);
        Epic epic = new Epic("Эпик 1", "Создание");
        Subtask subtask = new Subtask(1, "Тест", "Проверка обновления",
                TaskStatus.DONE, duration, date.minusDays(1));

        // Запрос на создание задачи
        responseManager.sendPostResponse("/tasks", task);

        // Запрос на создание эпика
        responseManager.sendPostResponse("/epics", epic);

        // Запрос на создание подзадачи
        responseManager.sendPostResponse("/subtasks", subtask);

        // Запрос на получение задачи по ID
        HttpResponse<String> taskResponse =  responseManager.sendGetResponse("/tasks/1");

        // Запрос на получение эпика по ID
        responseManager.sendGetResponse("/epics/2");

        // Запрос на получение подзадачи по ID
        HttpResponse<String> subtaskResponse = responseManager.sendGetResponse("/subtasks/3");

        // Запрос на удаление эпиков
        responseManager.sendDeleteResponse("/epics");

        // Запрос на получение списка истории
        HttpResponse<String> response = responseManager.sendGetResponse("/history");

        assertEquals(200, response.statusCode(), "Некорректный код статуса");


        Task receivedTask = gson.fromJson(taskResponse.body(), Task.class);
        Task receivedSubtask = gson.fromJson(subtaskResponse.body(), Task.class);
        List<Task> history = gson.fromJson(response.body(), new TypeTokens.TasksListTypeToken().getType());

        assertFalse(history.isEmpty(), "Список не должен быть пуст");
        assertEquals(1, history.size(), "В списке должно быть 2 задачи");
        assertTrue(history.contains(receivedTask),"В списке отсутствует задача");
        assertFalse(history.contains(receivedSubtask),"В списке присутствует подзадача");

        assertEquals(receivedTask, history.getFirst(), "Первой должна быть задача");
        assertEquals(receivedTask, history.getLast(), "Последней должна быть задача");
    }

    @Test // Проверка получения корректного списка истории при удалении подзадач
    void testGetCorrectHistoryListAfterSubtasksRemoval() throws IOException, InterruptedException {
        Task task = new Task("Тест 1", "Проверка создания", TaskStatus.NEW,
                duration, date);
        Epic epic = new Epic("Эпик 1", "Создание");
        Subtask subtask = new Subtask(1, "Тест", "Проверка обновления",
                TaskStatus.DONE, duration, date.minusDays(1));

        // Запрос на создание задачи
        responseManager.sendPostResponse("/tasks", task);

        // Запрос на создание эпика
        responseManager.sendPostResponse("/epics", epic);

        // Запрос на создание подзадачи
        responseManager.sendPostResponse("/subtasks", subtask);

        // Запрос на получение задачи по ID
        HttpResponse<String> taskResponse = responseManager.sendGetResponse("/tasks/1");

        // Запрос на получение эпика по ID
        HttpResponse<String> epicResponse = responseManager.sendGetResponse("/epics/2");

        // Запрос на получение подзадачи по ID
        responseManager.sendGetResponse("/subtasks/3");

        // Запрос на удаление подзадач
        responseManager.sendDeleteResponse("/subtasks");

        // Запрос на получение списка истории
        HttpResponse<String> response = responseManager.sendGetResponse("/history");

        assertEquals(200, response.statusCode(), "Некорректный код статуса");

        Task receivedTask = gson.fromJson(taskResponse.body(), Task.class);
        Task receivedEpic = gson.fromJson(epicResponse.body(), Task.class);
        List<Task> history = gson.fromJson(response.body(), new TypeTokens.TasksListTypeToken().getType());

        assertFalse(history.isEmpty(), "Список не должен быть пуст");
        assertEquals(2, history.size(), "В списке должно быть 3 задачи");
        assertTrue(history.contains(receivedTask),"В списке отсутствует задача");
        assertTrue(history.contains(receivedEpic),"В списке отсутствует эпик");

        assertEquals(receivedTask, history.getFirst(), "Первой должна быть задача");
        assertEquals(receivedEpic, history.getLast(), "Последним должен быть эпик");
    }
}
