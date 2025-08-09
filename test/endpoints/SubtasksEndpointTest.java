package endpoints;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
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

public class SubtasksEndpointTest {
    private final TaskManager manager = new InMemoryTaskManager();
    private final HttpTaskServer taskServer = new HttpTaskServer(manager);
    private final Gson gson = taskServer.getGson();
    private ResponseManager responseManager;
    private final LocalDateTime date = LocalDateTime.of(2025, 5, 12, 14, 54);
    private final long duration = 5;

    private SubtasksEndpointTest() {

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

    @Test // Проверка получения пустого списка подзадач
    void testGetEmptySubtaskList() throws IOException, InterruptedException {
        // Запрос на получение списка подзадач
        HttpResponse<String> response = responseManager.sendGetResponse("/subtasks");

        assertEquals(200, response.statusCode(), "Некорректный код статуса");

        List<Subtask> subtasks = gson.fromJson(response.body(), new TypeTokens.SubtasksListTypeToken().getType());
        assertTrue(subtasks.isEmpty(), "Список должен быть пуст");
    }

    @Test // Проверка создания подзадачи
    void testCreateSubtask() throws IOException, InterruptedException {
        Epic epic = new Epic("Эпик 1", "Создание");

        Subtask subtask = new Subtask("Тест 1 создание", "Подзадача", TaskStatus.NEW,
                duration, date);

        // Запрос на создание эпика
        responseManager.sendPostResponse("/epics", epic);

        // Запрос на получение списка эпиков
        HttpResponse<String> epicResponse = responseManager.sendGetResponse("/epics");

        List<Epic> receivedEpics = gson.fromJson(epicResponse.body(), new TypeToken<List<Epic>>(){}.getType());

        // Запрос на создание подзадачи
        HttpResponse<String> response = responseManager.sendPostResponse("/subtasks", subtask);

        assertEquals(201, response.statusCode(), "Некорректный код статуса");

        List<Subtask> subtasks = manager.getSubtasks();
        Subtask firstSubtask = subtasks.getFirst();

        assertEquals(1, subtasks.size(), "Некорректное количество подзадач");
        assertEquals(receivedEpics.getLast().getTaskId(), firstSubtask.getEpicIdentifier(),
                "Некорректный ID эпика у подзадачи");
        assertEquals(2, firstSubtask.getTaskId(), "Некорректный ID подзадачи");
        assertEquals("Тест 1 создание", firstSubtask.getTaskName(), "Некорректное имя подзадачи");
        assertEquals("Подзадача", firstSubtask.getTaskDescription(),
                "Некорректное описание подзадачи");
        assertEquals(TaskStatus.NEW, firstSubtask.getTaskStatus(), "Некорректный статус подзадачи");
        assertEquals(duration, firstSubtask.getDuration().toMinutes(), "Некорректная длительность подзадачи");
        assertEquals(date, firstSubtask.getStartTime(), "Некорректное время начала подзадачи");
    }

    @Test // Проверка выбрасывания исключения при попытке создать подзадачу с несуществующим эпиком
    void testCreateSubtaskWithNonExistentEpic() throws IOException, InterruptedException {
        Subtask subtask = new Subtask("Тест 1 создание", "Подзадача", TaskStatus.NEW,
                duration, date);
        subtask.setEpicIdentifier(888);

        // Запрос на создание подзадачи
        HttpResponse<String> response = responseManager.sendPostResponse("/subtasks", subtask);

        assertEquals(404, response.statusCode(), "Некорректный код статуса");
        assertNotNull(response.body(), "Сообщение об ошибке не должно быть null");
        assertFalse(response.body().isEmpty(), "Сообщение об ошибке не должно быть пустым");
    }

    @Test // Проверка выбрасывания исключения при попытке создать подзадачу без эпика
    void testCreateSubtaskWithoutEpic() throws IOException, InterruptedException {
        Subtask subtask = new Subtask("Тест 1 создание", "Подзадача", TaskStatus.NEW,
                duration, date);

        // Запрос на создание подзадачи
        HttpResponse<String> response = responseManager.sendPostResponse("/subtasks", subtask);

        assertEquals(404, response.statusCode(), "Некорректный код статуса");
        assertNotNull(response.body(), "Сообщение об ошибке не должно быть null");
        assertFalse(response.body().isEmpty(), "Сообщение об ошибке не должно быть пустым");
    }

    @Test // Проверка получения списка подзадач
    void testGetAllSubtasks() throws IOException, InterruptedException {
        Epic epic = new Epic("Эпик 1", "Создание");
        Subtask subtask = new Subtask("Тест 1", "Проверка создания", TaskStatus.NEW,
                duration, date);
        Subtask newSubtask = new Subtask("Тест 2", "Проверка создания 2", TaskStatus.DONE,
                duration, LocalDateTime.now().plusMinutes(180));

        // Запрос на создание эпика
        responseManager.sendPostResponse("/epics", epic);

        // Запрос на создание подзадачи
        responseManager.sendPostResponse("/subtasks", subtask);

        // Запрос на создание подзадачи
        responseManager.sendPostResponse("/subtasks", newSubtask);

        // Запрос на получение списка подзадач
        HttpResponse<String> response = responseManager.sendGetResponse("/subtasks");

        assertEquals(200, response.statusCode());
        List<Subtask> subtasks = gson.fromJson(response.body(), new TypeTokens.SubtasksListTypeToken().getType());
        assertFalse(subtasks.isEmpty(), "Список не должен быть пуст");
        assertEquals(2, subtasks.size(), "Некорректное количество подзадач");
    }

    @Test // Проверка выбрасывания исключения при попытке получить несуществующую подзадачу по ID
    void testGetNonExistentSubtaskByID() throws IOException, InterruptedException {
        // Запрос на получение подзадачи по ID
        HttpResponse<String> newResponse = responseManager.sendGetResponse("/subtasks/1");

        assertEquals(404, newResponse.statusCode(), "Некорректный код статуса");
        assertNotNull(newResponse.body(), "Сообщение об ошибке не должно быть null");
        assertFalse(newResponse.body().isEmpty(), "Сообщение об ошибке не должно быть пустым");
    }

    @Test // Проверка получения подзадачи по ID
    void testGetExistingSubtaskByID() throws IOException, InterruptedException {
        Epic epic = new Epic("Эпик 1", "Создание");
        Subtask subtask = new Subtask("Тест 1 создание", "Подзадача", TaskStatus.NEW,
                duration, date);

        // Запрос на создание эпика
        responseManager.sendPostResponse("/epics", epic);

        // Запрос на создание подзадачи
        responseManager.sendPostResponse("/subtasks", subtask);

        // Запрос на получение подзадачи по ID
        HttpResponse<String> response = responseManager.sendGetResponse("/subtasks/2");

        assertEquals(200, response.statusCode());
        Subtask firstSubtask = gson.fromJson(response.body(), Subtask.class);

        assertEquals(2, firstSubtask.getTaskId(), "Некорректный ID подзадачи");
        assertEquals(subtask.getTaskName(), firstSubtask.getTaskName(),
                "Некорректное имя подзадачи");
        assertEquals(subtask.getTaskDescription(), firstSubtask.getTaskDescription(),
                "Некорректное описание подзадачи");
        assertEquals(subtask.getTaskStatus(), firstSubtask.getTaskStatus(),
                "Некорректный статус подзадачи");
        assertEquals(subtask.getDuration(), firstSubtask.getDuration(),
                "Некорректная длительность подзадачи");
        assertEquals(date, firstSubtask.getStartTime(),
                "Некорректное время начала подзадачи");
    }

    @Test // Проверка удаления всех подзадач из пустого списка
    void testDeleteAllSubtasksWhenTasksListIsEmpty() throws  IOException, InterruptedException {
        // Запрос на удаление подзадач
        HttpResponse<String> response = responseManager.sendDeleteResponse("/subtasks");

        assertEquals(201, response.statusCode(), "Некорректный код статуса");
        assertTrue(manager.getSubtasks().isEmpty(), "Список должен быть пуст");
    }

    @Test // Проверка удаления всех подзадач
    void testDeleteAllSubtasks() throws IOException, InterruptedException {
        Epic epic = new Epic("Эпик 1", "Создание");
        Subtask subtask = new Subtask("Тест 1", "Проверка создания", TaskStatus.NEW,
                duration, date);
        Subtask newSubtask = new Subtask("Тест 2", "Проверка создания 2", TaskStatus.DONE,
                duration, LocalDateTime.now().plusMinutes(180));

        // Запрос на создание эпика
        responseManager.sendPostResponse("/epics", epic);

        // Запрос на создание подзадачи
        responseManager.sendPostResponse("/subtasks", subtask);

        // Запрос на создание подзадачи
        responseManager.sendPostResponse("/subtasks", newSubtask);

        // Запрос на удаление подзадач
        HttpResponse<String> response = responseManager.sendDeleteResponse("/subtasks");

        assertEquals(201, response.statusCode(), "Некорректный код статуса");
        assertTrue(manager.getSubtasks().isEmpty(), "Список должен быть пуст");
    }

    @Test // Проверка выбрасывания исключения при попытке удалить несуществующую подзадачу по ID
    void testDeleteNonExistentSubtaskById() throws IOException, InterruptedException {
        int taskId = 888;

        // Запрос на удаление подзадачи по ID
        HttpResponse<String> newResponse = responseManager.sendDeleteResponse("/subtasks/" + taskId);

        assertEquals(404, newResponse.statusCode(), "Некорректный код статуса");
        assertNotNull(newResponse.body(), "Сообщение об ошибке не должно быть null");
        assertFalse(newResponse.body().isEmpty(), "Сообщение об ошибке не должно быть пустым");
    }

    @Test // Проверка удаления подзадачи по ID
    void testDeleteSubtaskByID() throws IOException, InterruptedException {
        Epic epic = new Epic("Эпик 1", "Создание");
        Subtask subtask = new Subtask("Тест 1 создание", "Подзадача", TaskStatus.NEW,
                duration, date);

        // Запрос на создание эпика
        responseManager.sendPostResponse("/epics", epic);

        // Запрос на создание подзадачи
        responseManager.sendPostResponse("/subtasks", subtask);

        // Запрос на удаление подзадачи по ID
        HttpResponse<String> newResponse = responseManager.sendDeleteResponse("/subtasks/2");

        assertEquals(200, newResponse.statusCode(), "Некорректный код статуса");
        assertTrue(manager.getSubtasks().isEmpty(), "Список должен быть пуст");

        Subtask firstSubtask  = gson.fromJson(newResponse.body(), Subtask.class);

        assertEquals(2, firstSubtask.getTaskId(), "Некорректный ID задачи");
        assertEquals(subtask.getTaskName(), firstSubtask.getTaskName(), "Некорректное имя задачи");
        assertEquals(subtask.getTaskDescription(), firstSubtask.getTaskDescription(),
                "Некорректное описание задачи");
        assertEquals(subtask.getTaskStatus(), firstSubtask.getTaskStatus(), "Некорректный статус задачи");
        assertEquals(subtask.getDuration(), firstSubtask.getDuration(), "Некорректная длительность задачи");
        assertEquals(date, firstSubtask.getStartTime(), "Некорректное время начала подзадачи");
    }

    @Test // Проверка выбрасывания исключения при попытке добавить пересекающуюся подзадачу
    void shouldNotCreateIntersectSubtask() throws IOException, InterruptedException {
        Epic epic = new Epic("Эпик 1", "Создание");
        Subtask subtask = new Subtask("Тест 1", "Проверка создания",
                TaskStatus.NEW, duration, date);
        Subtask newSubtask = new Subtask("Тест 2", "Проверка создания 2",
                TaskStatus.DONE, duration, date.plusMinutes(4));

        // Запрос на создание эпика
        responseManager.sendPostResponse("/epics", epic);

        // Запрос на создание подзадачи
        responseManager.sendPostResponse("/subtasks", subtask);

        // Запрос на создание подзадачи
        HttpResponse<String> newResponse = responseManager.sendPostResponse("/subtasks", newSubtask);

        assertEquals(406, newResponse.statusCode(), "Некорректный код статуса");
        assertNotNull(newResponse.body(), "Сообщение об ошибке не должно быть null");
        assertFalse(newResponse.body().isEmpty(), "Сообщение об ошибке не должно быть пустым");
    }

    @Test // Проверка обновления существующей подзадачи
    void shouldUpdateExistingSubtask() throws IOException, InterruptedException {
        Epic epic = new Epic("Эпик 1", "Создание");
        Subtask subtask = new Subtask("Тест 1", "Проверка создания", TaskStatus.NEW,
                duration, date);
        Subtask newSubtask = new Subtask(2,"Тест 2", "Проверка создания 2",
                TaskStatus.DONE, duration, date.plusMinutes(3));

        // Запрос на создание эпика
        responseManager.sendPostResponse("/epics", epic);

        // Запрос на создание подзадачи
        responseManager.sendPostResponse("/subtasks", subtask);

        // Запрос на обновление подзадачи
        HttpResponse<String> updateResponse = responseManager.sendPostResponse("/subtasks/2", newSubtask);

        assertEquals(201, updateResponse.statusCode(), "Некорректный код статуса");

        // Запрос на получение списка подзадач
        HttpResponse<String> response = responseManager.sendGetResponse("/subtasks");

        List<Subtask> subtasks = gson.fromJson(response.body(), new TypeTokens.SubtasksListTypeToken().getType());
        Subtask firstSubtask = subtasks.getFirst();

        assertFalse(subtasks.isEmpty(), "Список не должен быть пуст");
        assertEquals(1, subtasks.size(), "Некорректное количество подзадач");
        assertEquals(newSubtask.getTaskName(), firstSubtask.getTaskName(),
                "Некорректное имя подзадачи");
        assertEquals(newSubtask.getTaskDescription(), firstSubtask.getTaskDescription(),
                "Некорректное описание подзадачи");
        assertEquals(newSubtask.getTaskStatus(), firstSubtask.getTaskStatus(),
                "Некорректный статус подзадачи");
        assertEquals(newSubtask.getDuration(), firstSubtask.getDuration(),
                "Некорректная длительность подзадачи");
        assertEquals(date.plusMinutes(3), firstSubtask.getStartTime(),
                "Некорректное время начала подзадачи");
    }

    @Test // Проверка выбрасывания исключения при попытке обновить подзадачу, вызывающей пересечение задач
    void shouldNotUpdateIntersectSubtask() throws IOException, InterruptedException {
        Task task = new Task("Тест 1", "Проверка создания", TaskStatus.NEW,
                duration, date);
        Epic epic = new Epic("Эпик 1", "Создание");
        Subtask subtask = new Subtask(1, "Тест", "Проверка обновления",
                TaskStatus.DONE, duration, date.plusMinutes(6));
        Subtask newSubtask = new Subtask(3,"Тест", "Проверка",
                TaskStatus.IN_PROGRESS, duration, date.plusMinutes(3));

        // Запрос на создание задачи
        responseManager.sendPostResponse("/tasks", task);

        // Запрос на создание эпика
        responseManager.sendPostResponse("/epics", epic);

        // Запрос на создание подзадачи
        responseManager.sendPostResponse("/subtasks", subtask);

        // Запрос на обновление подзадачи
        HttpResponse<String> response = responseManager.sendPostResponse("/subtasks/3", newSubtask);

        assertEquals(406, response.statusCode(), "Некорректный код статуса");
        assertNotNull(response.body(), "Сообщение об ошибке не должно быть null");
        assertFalse(response.body().isEmpty(), "Сообщение об ошибке не должно быть пустым");
    }

    @Test // Проверка выбрасывания исключения при попытке обновить несуществующую подзадачу
    void shouldNotUpdateNonExistentSubtask() throws IOException, InterruptedException {
        Subtask subtask = new Subtask(1,"Тест 1 создание", "Подзадача", TaskStatus.NEW,
                duration, date);

        // Запрос на обновление подзадачи
        HttpResponse<String> response = responseManager.sendPostResponse("/subtasks/1", subtask);

        assertEquals(404, response.statusCode(), "Некорректный код статуса");
        assertNotNull(response.body(), "Сообщение об ошибке не должно быть null");
        assertFalse(response.body().isEmpty(), "Сообщение об ошибке не должно быть пустым");
    }
}

