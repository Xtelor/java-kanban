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

public class PriorityListEndpointTest {
    private final TaskManager manager = new InMemoryTaskManager();
    private final HttpTaskServer taskServer = new HttpTaskServer(manager);
    private final Gson gson = taskServer.getGson();
    private ResponseManager responseManager;
    private final LocalDateTime date = LocalDateTime.of(2025, 5, 12, 14, 54);
    private final long duration = 5;

    private PriorityListEndpointTest() {

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

    @Test // Проверка получения пустого приоритетного списка
    void testGetEmptyPrioritizedTasksList() throws IOException, InterruptedException {
        // Запрос на получение приоритетного списка
        HttpResponse<String> response = responseManager.sendGetResponse("/prioritized");

        assertEquals(200, response.statusCode(), "Некорректный код статуса");

        List<Task> priority = gson.fromJson(response.body(), new TypeTokens.TasksListTypeToken().getType());
        assertTrue(priority.isEmpty(), "Список должен быть пуст");
    }

    @Test // Проверка получения корректного приоритетного списка
    void testGetCorrectPrioritizedList() throws IOException, InterruptedException {
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

        // Запрос на получение приоритетного списка
        HttpResponse<String> response = responseManager.sendGetResponse("/prioritized");

        assertEquals(200, response.statusCode(), "Некорректный код статуса");


        List<Task> priority = gson.fromJson(response.body(), new TypeTokens.TasksListTypeToken().getType());

        Task receivedTask = manager.getTasks().getFirst();
        Task receivedSubtask = manager.getSubtasks().getFirst();

        assertFalse(priority.isEmpty(), "Список не должен быть пуст");
        assertEquals(2, priority.size(), "В списке должно быть 2 задачи");
        assertTrue(priority.contains(receivedTask),"В списке отсутствует задача");
        assertEquals(receivedTask, priority.getLast(), "Последней должна быть задача");
    }

    @Test // Проверка получения корректного приоритетного списка при удалении подзадачи
    void testGetCorrectPrioritizedListAfterSubtaskRemoval() throws IOException, InterruptedException {
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

        // Запрос на удаление подзадачи по ID
        responseManager.sendDeleteResponse("/subtasks/3");

        // Запрос на получение приоритетного списка
        HttpResponse<String> response = responseManager.sendGetResponse("/prioritized");

        assertEquals(200, response.statusCode(), "Некорректный код статуса");


        List<Task> priority = gson.fromJson(response.body(), new TypeTokens.TasksListTypeToken().getType());

        Task receivedTask = manager.getTasks().getFirst();

        assertFalse(priority.isEmpty(), "Список не должен быть пуст");
        assertEquals(1, priority.size(), "В списке должна быть 1 задача");
        assertTrue(priority.contains(receivedTask),"В списке отсутствует задача");
        assertEquals(receivedTask, priority.getFirst(), "Первой должна быть задача");
        assertEquals(receivedTask, priority.getLast(), "Последней должна быть задача");
    }

    @Test // Проверка получения корректного приоритетного списка при удалении подзадач
    void testGetCorrectPrioritizedListAfterSubtasksRemoval() throws IOException, InterruptedException {
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

        // Запрос на удаление подзадач
        responseManager.sendDeleteResponse("/subtasks");

        // Запрос на получение приоритетного списка
        HttpResponse<String> response = responseManager.sendGetResponse("/prioritized");

        assertEquals(200, response.statusCode(), "Некорректный код статуса");

        assertEquals(200, response.statusCode(), "Некорректный код статуса");


        List<Task> priority = gson.fromJson(response.body(), new TypeTokens.TasksListTypeToken().getType());

        Task receivedTask = manager.getTasks().getFirst();

        assertFalse(priority.isEmpty(), "Список не должен быть пуст");
        assertEquals(1, priority.size(), "В списке должна быть 1 задача");
        assertTrue(priority.contains(receivedTask),"В списке отсутствует задача");
        assertEquals(receivedTask, priority.getFirst(), "Первой должна быть задача");
        assertEquals(receivedTask, priority.getLast(), "Последней должна быть задача");
    }

    @Test // Проверка получения корректного приоритетного списка при удалении задачи
    void testGetCorrectPrioritizedListAfterTaskRemoval() throws IOException, InterruptedException {
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

        // Запрос на удаление задачи по ID
        responseManager.sendDeleteResponse("/tasks/1");

        // Запрос на получение приоритетного списка
        HttpResponse<String> response = responseManager.sendGetResponse("/prioritized");

        assertEquals(200, response.statusCode(), "Некорректный код статуса");


        List<Task> priority = gson.fromJson(response.body(), new TypeTokens.TasksListTypeToken().getType());

        assertFalse(priority.isEmpty(), "Список не должен быть пуст");
        assertEquals(1, priority.size(), "В списке должна быть 1 задача");
        assertTrue(manager.getTasks().isEmpty(),"В списке присутствует задача");
    }

    @Test // Проверка получения корректного приоритетного списка при удалении задач
    void testGetCorrectPrioritizedListAfterTasksRemoval() throws IOException, InterruptedException {
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

        // Запрос на удаление задач
        responseManager.sendDeleteResponse("/tasks");

        // Запрос на получение приоритетного списка
        HttpResponse<String> response = responseManager.sendGetResponse("/prioritized");

        assertEquals(200, response.statusCode(), "Некорректный код статуса");


        List<Task> priority = gson.fromJson(response.body(), new TypeTokens.TasksListTypeToken().getType());

        assertFalse(priority.isEmpty(), "Список не должен быть пуст");
        assertEquals(1, priority.size(), "В списке должна быть 1 задача");
        assertTrue(manager.getTasks().isEmpty(),"В списке присутствует задача");
    }

    @Test // Проверка получения корректного приоритетного списка при удалении эпика
    void testGetCorrectPrioritizedListAfterEpicRemoval() throws IOException, InterruptedException {
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

        // Запрос на удаление эпика по ID
        responseManager.sendDeleteResponse("/epics/2");

        // Запрос на получение приоритетного списка
        HttpResponse<String> response = responseManager.sendGetResponse("/prioritized");

        assertEquals(200, response.statusCode(), "Некорректный код статуса");

        List<Task> priority = gson.fromJson(response.body(), new TypeTokens.TasksListTypeToken().getType());

        Task receivedTask = manager.getTasks().getFirst();

        assertFalse(priority.isEmpty(), "Список не должен быть пуст");
        assertEquals(1, priority.size(), "В списке должна быть 1 задача");
        assertTrue(priority.contains(receivedTask),"В списке отсутствует задача");
        assertEquals(receivedTask, priority.getFirst(), "Первой должна быть задача");
        assertEquals(receivedTask, priority.getLast(), "Последней должна быть задача");
    }

    @Test // Проверка получения корректного приоритетного списка при удалении эпиков
    void testGetCorrectPrioritizedListAfterEpicsRemoval() throws IOException, InterruptedException {
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

        // Запрос на удаление эпиков
        responseManager.sendDeleteResponse("/epics");

        // Запрос на получение приоритетного списка
        HttpResponse<String> response = responseManager.sendGetResponse("/prioritized");

        assertEquals(200, response.statusCode(), "Некорректный код статуса");


        List<Task> priority = gson.fromJson(response.body(), new TypeTokens.TasksListTypeToken().getType());

        Task receivedTask = manager.getTasks().getFirst();

        assertFalse(priority.isEmpty(), "Список не должен быть пуст");
        assertEquals(1, priority.size(), "В списке должна быть 1 задача");
        assertTrue(priority.contains(receivedTask),"В списке отсутствует задача");
        assertEquals(receivedTask, priority.getFirst(), "Первой должна быть задача");
        assertEquals(receivedTask, priority.getLast(), "Последней должна быть задача");
    }

    @Test // Проверка получения корректного приоритетного списка при обновлении задачи
    void testGetCorrectPrioritizedListAfterTaskUpdate() throws IOException, InterruptedException {
        Task task = new Task("Тест 1", "Проверка создания", TaskStatus.NEW,
                duration, date);
        Epic epic = new Epic("Эпик 1", "Создание");
        Subtask subtask = new Subtask("Тест", "Проверка подзадачи",
                TaskStatus.DONE, duration, date.minusDays(1));
        Task newTask = new Task(1, "Тест 1", "Проверка создания", TaskStatus.DONE,
                duration, date.plusMinutes(3));

        // Запрос на создание задачи
        responseManager.sendPostResponse("/tasks", task);

        // Запрос на создание эпика
        responseManager.sendPostResponse("/epics", epic);

        // Запрос на создание подзадачи
        responseManager.sendPostResponse("/subtasks", subtask);

        // Запрос на обновление задачи
        responseManager.sendPostResponse("/tasks/1", newTask);

        // Запрос на получение приоритетного списка
        HttpResponse<String> response = responseManager.sendGetResponse("/prioritized");

        assertEquals(200, response.statusCode(), "Некорректный код статуса");

        List<Task> priority = gson.fromJson(response.body(), new TypeTokens.TasksListTypeToken().getType());

        assertFalse(priority.isEmpty(), "Список не должен быть пуст");
        assertEquals(2, priority.size(), "В списке должна быть 2 задачи");
        assertTrue(priority.contains(newTask),"В списке отсутствует задача");
        assertFalse(priority.contains(task),"В списке присутствует задача");
    }

    @Test // Проверка получения корректного приоритетного списка при обновлении подзадачи
    void testGetCorrectPrioritizedListAfterSubtaskUpdate() throws IOException, InterruptedException {
        Task task = new Task("Тест 1", "Проверка создания", TaskStatus.NEW,
                duration, date);
        Epic epic = new Epic("Эпик 1", "Создание");
        Subtask subtask = new Subtask("Тест", "Проверка подзадачи",
                TaskStatus.DONE, duration, date.minusDays(1));
        Subtask newSubtask = new Subtask(3, "Тест 1", "Проверка обновления",
                TaskStatus.IN_PROGRESS, duration, date.plusMinutes(60));

        // Запрос на создание задачи
        responseManager.sendPostResponse("/tasks", task);

        // Запрос на создание эпика
        responseManager.sendPostResponse("/epics", epic);

        // Запрос на создание подзадачи
        responseManager.sendPostResponse("/subtasks", subtask);

        // Запрос на обновление подзадачи
        responseManager.sendPostResponse("/subtasks/3", newSubtask);

        // Запрос на получение приоритетного списка
        HttpResponse<String> response = responseManager.sendGetResponse("/prioritized");

        assertEquals(200, response.statusCode(), "Некорректный код статуса");

        List<Task> priority = gson.fromJson(response.body(), new TypeTokens.TasksListTypeToken().getType());

        assertFalse(priority.isEmpty(), "Список не должен быть пуст");
        assertEquals(2, priority.size(), "В списке должна быть 2 задачи");
        assertFalse(priority.contains(subtask),"В списке присутствует подзадача");
    }
}
