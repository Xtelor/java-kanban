package endpoints;

import com.google.gson.Gson;
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


public class TasksEndpointTest  {

    private final TaskManager manager = new InMemoryTaskManager();
    private final HttpTaskServer taskServer = new HttpTaskServer(manager);
    private final Gson gson = taskServer.getGson();
    private ResponseManager responseManager;
    private final LocalDateTime date = LocalDateTime.of(2025, 5, 12, 14, 54);
    private final long duration = 5;

    private TasksEndpointTest() {

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

    @Test // Проверка получения пустого списка задач
    void testGetEmptyTaskList() throws IOException, InterruptedException {
        // Запрос на получение списка задач
        HttpResponse<String> response = responseManager.sendGetResponse("/tasks");

        assertEquals(200, response.statusCode(), "Некорректный код статуса");

        List<Task> tasks = gson.fromJson(response.body(), new TypeTokens.TasksListTypeToken().getType());
        assertTrue(tasks.isEmpty(), "Список должен быть пуст");
    }

    @Test // Проверка создания задачи
    void testCreateTask() throws IOException, InterruptedException {
        Task task = new Task("Тест 1", "Проверка создания", TaskStatus.NEW,
                duration, date);

        // Запрос на создание задачи
        HttpResponse<String> response = responseManager.sendPostResponse("/tasks", task);

        assertEquals(201, response.statusCode(), "Некорректный код статуса");

        List<Task> tasks = manager.getTasks();
        Task firstTask = tasks.getFirst();

        assertEquals(1, tasks.size(), "Некорректное количество задач");
        assertEquals(1, firstTask.getTaskId(), "Некорректный ID задачи");
        assertEquals("Тест 1", firstTask.getTaskName(), "Некорректное имя задачи");
        assertEquals("Проверка создания", firstTask.getTaskDescription(),
                "Некорректное описание задачи");
        assertEquals(TaskStatus.NEW, firstTask.getTaskStatus(), "Некорректный статус задачи");
        assertEquals(duration, firstTask.getDuration().toMinutes(), "Некорректная длительность задачи");
        assertEquals(date, firstTask.getStartTime(), "Некорректное время начала задачи");
    }

    @Test // Проверка получения списка задач
    void testGetAllTasks() throws IOException, InterruptedException {
        Task task = new Task("Тест 1", "Проверка создания", TaskStatus.NEW,
                duration, date);
        Task newTask = new Task("Тест 2", "Проверка создания 2", TaskStatus.DONE,
                duration, LocalDateTime.now().plusMinutes(180));

        // Запрос на создание задачи
        responseManager.sendPostResponse("/tasks", task);

        // Запрос на создание задачи
        responseManager.sendPostResponse("/tasks", newTask);

        // Запрос на получение списка задач
        HttpResponse<String> response = responseManager.sendGetResponse("/tasks");

        assertEquals(200, response.statusCode(), "Некорректный код статуса");
        assertFalse(manager.getTasks().isEmpty(), "Список не должен быть пуст");
    }

    @Test // Проверка выбрасывания исключения при попытке получить несуществующую задачу по ID
    void testGetNonExistentTaskByID() throws IOException, InterruptedException {
        // Запрос на получение задачи по ID
        HttpResponse<String> newResponse = responseManager.sendGetResponse("/tasks/1");

        assertEquals(404, newResponse.statusCode(), "Некорректный код статуса");
        assertNotNull(newResponse.body(), "Сообщение об ошибке не должно быть null");
        assertFalse(newResponse.body().isEmpty(), "Сообщение об ошибке не должно быть пустым");
    }

    @Test // Проверка получения задачи по ID
    void testGetExistingTaskByID() throws IOException, InterruptedException {
        Task task = new Task("Тест 1", "Проверка создания", TaskStatus.NEW,
                duration, date);

        // Запрос на создание задачи
        responseManager.sendPostResponse("/tasks", task);

        int taskId = 1;

        // Запрос на получение задачи по ID
        HttpResponse<String> newResponse = responseManager.sendGetResponse("/tasks/" + taskId);

        assertEquals(200, newResponse.statusCode(), "Некорректный код статуса");

        Task firstTask = gson.fromJson(newResponse.body(), Task.class);

        assertEquals(taskId, firstTask.getTaskId(), "Некорректный ID задачи");
        assertEquals(task.getTaskName(), firstTask.getTaskName(), "Некорректное имя задачи");
        assertEquals(task.getTaskDescription(), firstTask.getTaskDescription(),
                "Некорректное описание задачи");
        assertEquals(task.getTaskStatus(), firstTask.getTaskStatus(), "Некорректный статус задачи");
        assertEquals(task.getDuration(), firstTask.getDuration(), "Некорректная длительность задачи");
        assertEquals(task.getStartTime(), firstTask.getStartTime(), "Некорректное время начала задачи");
    }

    @Test // Проверка удаления всех задач из пустого списка
    void testDeleteAllTasksWhenTasksListIsEmpty() throws  IOException, InterruptedException {
        // Запрос на удаление задач
        HttpResponse<String> response = responseManager.sendDeleteResponse("/tasks");

        assertEquals(201, response.statusCode(), "Некорректный код статуса");
        assertTrue(manager.getTasks().isEmpty(), "Список должен быть пуст");
    }

    @Test // Проверка удаления всех задач
    void testDeleteAllTasks() throws IOException, InterruptedException {
        Task task = new Task("Тест 1", "Проверка создания", TaskStatus.NEW,
                duration, date);
        Task newTask = new Task("Тест 2", "Проверка создания 2", TaskStatus.DONE,
                duration, LocalDateTime.now().plusMinutes(180));

        // Запрос на создание задачи
        responseManager.sendPostResponse("/tasks", task);

        // Запрос на создание задачи
        responseManager.sendPostResponse("/tasks", newTask);

        // Запрос на удаление задач
        HttpResponse<String> newResponse = responseManager.sendDeleteResponse("/tasks");

        assertEquals(201, newResponse.statusCode(), "Некорректный код статуса");
        assertTrue(manager.getTasks().isEmpty(), "Список должен быть пуст");
    }

    @Test // Проверка выбрасывания исключения при попытке удалить несуществующую задачу по ID
    void testDeleteNonExistentTaskById() throws IOException, InterruptedException {
        int taskId = 888;

        // Запрос на удаление задачи по ID
        HttpResponse<String> newResponse = responseManager.sendDeleteResponse("/tasks/" + taskId);

        assertEquals(404, newResponse.statusCode(), "Некорректный код статуса");
        assertNotNull(newResponse.body(), "Сообщение об ошибке не должно быть null");
        assertFalse(newResponse.body().isEmpty(), "Сообщение об ошибке не должно быть пустым");
    }

    @Test // Проверка удаления задачи по ID
    void testDeleteTaskByID() throws IOException, InterruptedException {
        Task task = new Task("Тест 1", "Проверка создания", TaskStatus.NEW,
                duration, date);

        // Запрос на создание задачи
        responseManager.sendPostResponse("/tasks", task);

        // Запрос на удаление задачи по ID
        HttpResponse<String> newResponse = responseManager.sendDeleteResponse("/tasks/1");

        assertEquals(200, newResponse.statusCode(), "Некорректный код статуса");
        assertTrue(manager.getTasks().isEmpty(), "Список должен быть пуст");

        Task firstTask = gson.fromJson(newResponse.body(), Task.class);

        assertEquals(1, firstTask.getTaskId(), "Некорректный ID задачи");
        assertEquals(task.getTaskName(), firstTask.getTaskName(), "Некорректное имя задачи");
        assertEquals(task.getTaskDescription(), firstTask.getTaskDescription(),
                "Некорректное описание задачи");
        assertEquals(task.getTaskStatus(), firstTask.getTaskStatus(), "Некорректный статус задачи");
        assertEquals(task.getDuration(), firstTask.getDuration(), "Некорректная длительность задачи");
        assertEquals(task.getStartTime(), firstTask.getStartTime(), "Некорректное время начала задачи");

    }

    @Test // Проверка выбрасывания исключения при попытке добавить пересекающуюся задачу
    void shouldNotCreateIntersectTask() throws IOException, InterruptedException {
        Task task = new Task("Тест 1", "Проверка создания", TaskStatus.NEW,
                duration, LocalDateTime.now());
        Task newTask = new Task("Тест 2", "Проверка создания 2", TaskStatus.DONE,
                duration, LocalDateTime.now().plusMinutes(4));

        // Запрос на создание задачи
        HttpResponse<String> response = responseManager.sendPostResponse("/tasks", task);

        assertEquals(201, response.statusCode(), "Некорректный код статуса");

        // Запрос на создание задачи
        HttpResponse<String> newResponse = responseManager.sendPostResponse("/tasks", newTask);

        assertEquals(406, newResponse.statusCode(), "Некорректный код статуса");
        assertNotNull(newResponse.body(), "Сообщение об ошибке не должно быть null");
        assertFalse(newResponse.body().isEmpty(), "Сообщение об ошибке не должно быть пустым");
    }

    @Test // Проверка обновления существующей подзадачи
    void shouldUpdateExistingTask() throws IOException, InterruptedException {
        Task task = new Task("Тест 1", "Проверка создания", TaskStatus.NEW,
                duration, date);
        Task newTask = new Task(1, "Тест", "Проверка обновления", TaskStatus.DONE,
                duration, date.plusMinutes(1));

        // Запрос на создание задачи
        responseManager.sendPostResponse("/tasks", task);

        // Запрос на обновление задачи
        HttpResponse<String> updateResponse = responseManager.sendPostResponse("/tasks/1", newTask);

        assertEquals(201, updateResponse.statusCode(), "Некорректный код статуса");

        int taskId = 1;

        // Запрос на получение списка задач
        HttpResponse<String> response = responseManager.sendGetResponse("/tasks/" + taskId);

        assertEquals(200, response.statusCode(), "Некорректный код статуса");

        Task firstTask = gson.fromJson(response.body(), Task.class);

        assertEquals(taskId, firstTask.getTaskId(), "Некорректный ID задачи");
        assertEquals(newTask.getTaskName(), firstTask.getTaskName(), "Некорректное имя задачи");
        assertEquals(newTask.getTaskDescription(), firstTask.getTaskDescription(),
                "Некорректное описание задачи");
        assertEquals(newTask.getTaskStatus(), firstTask.getTaskStatus(), "Некорректный статус задачи");
        assertEquals(newTask.getDuration(), firstTask.getDuration(), "Некорректная длительность задачи");
        assertEquals(newTask.getStartTime(), firstTask.getStartTime(), "Некорректное время начала задачи");
    }

    @Test // Проверка выбрасывания исключения при попытке обновить задачу, вызывающей пересечение задач
    void shouldNotUpdateIntersectTask() throws IOException, InterruptedException {
        Task task = new Task("Тест 1", "Проверка создания", TaskStatus.NEW,
                duration, date);
        Task newTask = new Task(1, "Тест", "Проверка обновления", TaskStatus.DONE,
                duration, date.plusMinutes(3));
        Task anotherTask = new Task("Тест", "Проверка", TaskStatus.IN_PROGRESS,
                duration, date.plusMinutes(6));

        // Запрос на создание задачи
        responseManager.sendPostResponse("/tasks", task);

        // Запрос на создание задачи
        responseManager.sendPostResponse("/tasks", anotherTask);

        // Запрос на обновление задачи по ID
        HttpResponse<String> response = responseManager.sendPostResponse("/tasks/1", newTask);

        assertEquals(406, response.statusCode(), "Некорректный код статуса");
        assertNotNull(response.body(), "Сообщение об ошибке не должно быть null");
        assertFalse(response.body().isEmpty(), "Сообщение об ошибке не должно быть пустым");
    }

    @Test // Проверка выбрасывания исключения при попытке обновить несуществующую задачу
    void shouldNotUpdateNonExistentTask() throws IOException, InterruptedException {
        Task newTask = new Task(1, "Тест", "Проверка обновления", TaskStatus.DONE,
                duration, date.plusMinutes(3));

        // Запрос на обновление задачи по ID
        HttpResponse<String> response = responseManager.sendPostResponse("/tasks/1", newTask);

        assertEquals(404, response.statusCode(), "Некорректный код статуса");
        assertNotNull(response.body(), "Сообщение об ошибке не должно быть null");
        assertFalse(response.body().isEmpty(), "Сообщение об ошибке не должно быть пустым");
    }
}