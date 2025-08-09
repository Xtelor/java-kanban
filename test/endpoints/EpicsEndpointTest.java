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
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.*;

public class EpicsEndpointTest {
    private final TaskManager manager = new InMemoryTaskManager();
    private final HttpTaskServer taskServer = new HttpTaskServer(manager);
    private final Gson gson = taskServer.getGson();
    private final LocalDateTime date = LocalDateTime.of(2025, 5, 12, 14, 54);
    private ResponseManager responseManager;

    private EpicsEndpointTest() {

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

    @Test // Проверка получения пустого списка эпиков
    void testGetEmptyEpicList() throws IOException, InterruptedException {
        // Запрос на получение списка эпиков
        HttpResponse<String> response = responseManager.sendGetResponse("/epics");

        assertEquals(200, response.statusCode(), "Некорректный код статуса");

        List<Epic> epics = gson.fromJson(response.body(), new TypeTokens.EpicsListTypeToken().getType());
        assertTrue(epics.isEmpty(), "Список должен быть пуст");
    }

    @Test // Проверка создания эпика
    void testCreateEpic() throws IOException, InterruptedException {
        Epic epic = new Epic("Тест 1", "Проверка создания");

        // Запрос на создание эпика
        HttpResponse<String> response = responseManager.sendPostResponse("/epics", epic);

        assertEquals(201, response.statusCode(), "Некорректный код статуса");

        List<Epic> epics = manager.getEpics();
        Task firstEpic = epics.getFirst();

        assertEquals(1, epics.size(), "Некорректное количество эпиков");
        assertEquals(1, firstEpic.getTaskId(), "Некорректный ID эпика");
        assertEquals("Тест 1", firstEpic.getTaskName(), "Некорректное имя эпика");
        assertEquals("Проверка создания", firstEpic.getTaskDescription(),
                "Некорректное описание эпика");
        assertEquals(TaskStatus.NEW, firstEpic.getTaskStatus(), "Некорректный статус эпика");
        assertEquals(0, firstEpic.getDuration().toMinutes(), "Некорректная длительность эпика");
        assertEquals(LocalDateTime.MIN, firstEpic.getStartTime(), "Некорректное время начала эпика");
    }

    @Test // Проверка получения списка эпиков
    void testGetAllEpics() throws IOException, InterruptedException {
        Epic epic = new Epic("Тест 1", "Проверка создания");
        Epic newEpic = new Epic("Тест 2", "Проверка создания 2");

        // Запрос на создание эпика
        responseManager.sendPostResponse("/epics", epic);

        // Запрос на создание эпика
        responseManager.sendPostResponse("/epics", newEpic);

        // Запрос на получения списка эпиков
        HttpResponse<String> response = responseManager.sendGetResponse("/epics");

        assertEquals(200, response.statusCode(), "Некорректный код статуса");

        List<Epic> epics = gson.fromJson(response.body(), new TypeTokens.EpicsListTypeToken().getType());
        assertFalse(epics.isEmpty(), "Список не должен быть пуст");
    }

    @Test // Проверка выбрасывания исключения при попытке получить несуществующий эпик по ID
    void testGetNonExistentEpicByID() throws IOException, InterruptedException {
        // Запрос на получение эпика по ID
        HttpResponse<String> newResponse = responseManager.sendGetResponse("/epics/1");

        assertEquals(404, newResponse.statusCode(), "Некорректный код статуса");
        assertNotNull(newResponse.body(), "Сообщение об ошибке не должно быть null");
        assertFalse(newResponse.body().isEmpty(), "Сообщение об ошибке не должно быть пустым");
    }

    @Test // Проверка получения эпика по ID
    void testGetExistingEpicByID() throws IOException, InterruptedException {
        Epic epic = new Epic("Тест 1", "Проверка создания");

        // Запрос на создание эпика
        responseManager.sendPostResponse("/epics", epic);

        int epicId = 1;

        // Запрос на получение эпика по ID
        HttpResponse<String> newResponse = responseManager.sendGetResponse("/epics/" + epicId);

        assertEquals(200, newResponse.statusCode(), "Некорректный код статуса");

        Epic firstEpic = gson.fromJson(newResponse.body(), Epic.class);

        assertEquals(epicId, firstEpic.getTaskId(), "Некорректный ID эпика");
        assertEquals(epic.getTaskName(), firstEpic.getTaskName(),
                "Некорректное имя эпика");
        assertEquals(epic.getTaskDescription(), firstEpic.getTaskDescription(),
                "Некорректное описание эпика");
        assertEquals(epic.getTaskStatus(), firstEpic.getTaskStatus(),
                "Некорректный статус эпика");
        assertEquals(epic.getDuration(), firstEpic.getDuration(),
                "Некорректная длительность эпика");
        assertEquals(epic.getStartTime(), firstEpic.getStartTime(),
                "Некорректное время начала эпика");
    }

    @Test // Проверка удаления всех эпиков из пустого списка
    void testDeleteAllTasksWhenEpicsListIsEmpty() throws  IOException, InterruptedException {
        // Запрос на удаление эпиков
        HttpResponse<String> response = responseManager.sendDeleteResponse("/epics");

        assertEquals(201, response.statusCode(), "Некорректный код статуса");
        assertTrue(manager.getEpics().isEmpty(), "Список должен быть пуст");
    }

    @Test // Проверка удаления всех эпиков
    void testDeleteAllEpics() throws IOException, InterruptedException {
        Epic epic = new Epic("Тест 1", "Проверка создания");
        Epic newEpic = new Epic("Тест 2", "Проверка создания 2");

        // Запрос на создание эпика
        responseManager.sendPostResponse("/epics", epic);

        // Запрос на создание эпика
        responseManager.sendPostResponse("/epics", newEpic);

        // Запрос на удаление эпиков
        HttpResponse<String> newResponse = responseManager.sendDeleteResponse("/epics");

        assertEquals(201, newResponse.statusCode(), "Некорректный код статуса");
        assertTrue(manager.getEpics().isEmpty(), "Список должен быть пуст");
    }

    @Test // Проверка выбрасывания исключения при попытке удалить несуществующий эпик по ID
    void testDeleteNonExistentEpicById() throws IOException, InterruptedException {
        int taskId = 888;

        // Запрос на удаление эпика по ID
        HttpResponse<String> newResponse = responseManager.sendDeleteResponse("/epics/" + taskId);

        assertEquals(404, newResponse.statusCode(), "Некорректный код статуса");
        assertNotNull(newResponse.body(), "Сообщение об ошибке не должно быть null");
        assertFalse(newResponse.body().isEmpty(), "Сообщение об ошибке не должно быть пустым");
    }

    @Test // Проверка удаления эпика по ID
    void testDeleteEpicByID() throws IOException, InterruptedException {
        Epic epic = new Epic("Тест 1", "Проверка создания");

        // Запрос на создание эпика
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/epics"))
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(epic)))
                .build();
        responseManager.sendPostResponse("/epics", epic);

        // Запрос на удаление эпика по ID
        HttpResponse<String> newResponse = responseManager.sendDeleteResponse("/epics/1");

        assertEquals(200, newResponse.statusCode(), "Некорректный код статуса");
        assertTrue(manager.getEpics().isEmpty());

        Epic firstEpic = gson.fromJson(newResponse.body(), Epic.class);

        assertEquals(1, firstEpic.getTaskId(), "Некорректный ID эпика");
        assertEquals(epic.getTaskName(), firstEpic.getTaskName(), "Некорректное имя эпика");
        assertEquals(epic.getTaskDescription(), firstEpic.getTaskDescription(),
                "Некорректное описание эпика");
        assertEquals(epic.getTaskStatus(), firstEpic.getTaskStatus(), "Некорректный статус эпика");
        assertEquals(epic.getDuration(), firstEpic.getDuration(), "Некорректная длительность эпика");
        assertEquals(epic.getStartTime(), firstEpic.getStartTime(), "Некорректное время начала эпика");
    }

    @Test // Проверка получения пустого списка подзадач эпика
    void shouldReturnEmptySubtasksList() throws IOException, InterruptedException {
        Epic epic = new Epic("Тест 1", "Проверка создания");

        // Запрос на создание эпика
        responseManager.sendPostResponse("/epics", epic);

        // Запрос на получение списка подзадач эпика
        HttpResponse<String> newResponse = responseManager.sendGetResponse("/epics/1/subtasks");

        assertEquals(200, newResponse.statusCode(), "Некорректный код статуса");
        List<Subtask> subtasks = gson.fromJson(newResponse.body(), new TypeTokens.SubtasksListTypeToken().getType());
        assertTrue(subtasks.isEmpty(), "Список должен быть пуст");
    }

    @Test // Проверка выбрасывания исключения при попытке получить список подзадач несуществующего эпика
    void testGetSubtasksListFromNonExistentEpic() throws IOException, InterruptedException {
        // Запрос на получение списка подзадач эпика
        HttpResponse<String> newResponse = responseManager.sendGetResponse("/epics/1/subtasks");

        assertEquals(404, newResponse.statusCode(), "Некорректный код статуса");
        assertNotNull(newResponse.body(), "Сообщение об ошибке не должно быть null");
        assertFalse(newResponse.body().isEmpty(), "Сообщение об ошибке не должно быть пустым");
    }

    @Test // Проверка получения списка подзадач эпика
    void testGetSubtasksList() throws IOException, InterruptedException {
        Epic epic = new Epic("Эпик 1", "Создание");
        long duration = 5;
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

        // Запрос на получение списка подзадач эпика
        HttpResponse<String> response = responseManager.sendGetResponse("/epics/1/subtasks");

        assertEquals(200, response.statusCode(), "Некорректный код статуса");
        List<Subtask> subtasks = gson.fromJson(response.body(), new TypeTokens.SubtasksListTypeToken().getType());
        assertFalse(subtasks.isEmpty(), "Список не должен быть пуст");
        assertEquals(2, subtasks.size(), "Некорректное количество подзадач");
    }

    @Test // Проверка обновления эпика с одной подзадачей
    void testEpicUpdateWithOneSubtask() throws IOException, InterruptedException {
        Epic epic = new Epic("Эпик 1", "Создание");
        long duration = 5;
        Subtask subtask = new Subtask("Тест 1", "Проверка создания", TaskStatus.NEW,
                duration, date);

        // Запрос на создание эпика
        responseManager.sendPostResponse("/epics", epic);

        // Запрос на создание подзадачи
        responseManager.sendPostResponse("/subtasks", subtask);

        // Запрос на получение эпика
        HttpResponse<String> response = responseManager.sendGetResponse("/epics/1");

        assertEquals(200, response.statusCode(), "Некорректный код статуса");
        Epic receivedEpic = gson.fromJson(response.body(), Epic.class);

        assertEquals(1, receivedEpic.getTaskId(), "Некорректный ID эпика");
        assertEquals(epic.getTaskName(), receivedEpic.getTaskName(),
                "Некорректное имя эпика");
        assertEquals(epic.getTaskDescription(), receivedEpic.getTaskDescription(),
                "Некорректное описание эпика");
        assertEquals(subtask.getTaskStatus(), receivedEpic.getTaskStatus(),
                "Некорректный статус эпика");
        assertEquals(subtask.getDuration(), receivedEpic.getDuration(),
                "Некорректная длительность эпика");
        assertEquals(subtask.getStartTime(), receivedEpic.getStartTime(),
                "Некорректное время начала эпика");
    }

    @Test // Проверка обновления эпика с несколькими подзадачами
    void testEpicUpdateWithSubtasks() throws IOException,InterruptedException {
        Epic epic = new Epic("Эпик 1", "Создание");
        long duration = 5;
        Subtask subtask = new Subtask("Тест 1", "Проверка создания", TaskStatus.NEW,
                duration, date);
        Subtask newSubtask = new Subtask("Тест 2", "Проверка создания 2", TaskStatus.DONE,
                duration, date.plusMinutes(180));

        // Запрос на создание эпика
        responseManager.sendPostResponse("/epics", epic);

        // Запрос на создание подзадачи
        responseManager.sendPostResponse("/subtasks", subtask);

        // Запрос на создание подзадачи
        responseManager.sendPostResponse("/subtasks", newSubtask);

        // Запрос на получение эпика
        HttpResponse<String> response = responseManager.sendGetResponse("/epics/1");

        assertEquals(200, response.statusCode(), "Некорректный код статуса");
        Epic receivedEpic = gson.fromJson(response.body(), Epic.class);

        assertEquals(1, receivedEpic.getTaskId(), "Некорректный ID эпика");
        assertEquals(epic.getTaskName(), receivedEpic.getTaskName(),
                "Некорректное имя эпика");
        assertEquals(epic.getTaskDescription(), receivedEpic.getTaskDescription(),
                "Некорректное описание эпика");
        assertEquals(TaskStatus.IN_PROGRESS, receivedEpic.getTaskStatus(),
                "Некорректный статус эпика");
        assertEquals(subtask.getDuration().plus(newSubtask.getDuration()), receivedEpic.getDuration(),
                "Некорректная длительность эпика");
        assertEquals(subtask.getStartTime(), receivedEpic.getStartTime(),
                "Некорректное время начала эпика");
    }

    @Test // Проверка обновления эпика при удалении подзадачи
    void testEpicUpdateWithSubtaskRemoval() throws IOException, InterruptedException {
        Epic epic = new Epic("Эпик 1", "Создание");
        long duration = 5;
        Subtask subtask = new Subtask("Тест 1", "Проверка создания", TaskStatus.NEW,
                duration, date);
        Subtask newSubtask = new Subtask("Тест 2", "Проверка создания 2", TaskStatus.DONE,
                duration, date.plusMinutes(180));

        // Запрос на создание эпика
        responseManager.sendPostResponse("/epics", epic);

        // Запрос на создание подзадачи
        responseManager.sendPostResponse("/subtasks", subtask);

        // Запрос на создание подзадачи
        responseManager.sendPostResponse("/subtasks", newSubtask);

        // Запрос на удаление подзадачи
        responseManager.sendDeleteResponse("/subtasks/2");

        // Запрос на получение эпика
        HttpResponse<String> response = responseManager.sendGetResponse("/epics/1");

        assertEquals(200, response.statusCode(), "Некорректный код статуса");
        Epic receivedEpic = gson.fromJson(response.body(), Epic.class);

        assertEquals(1, receivedEpic.getTaskId(), "Некорректный ID эпика");
        assertEquals(epic.getTaskName(), receivedEpic.getTaskName(),
                "Некорректное имя эпика");
        assertEquals(epic.getTaskDescription(), receivedEpic.getTaskDescription(),
                "Некорректное описание эпика");
        assertEquals(newSubtask.getTaskStatus(), receivedEpic.getTaskStatus(),
                "Некорректный статус эпика");
        assertEquals(newSubtask.getDuration(), receivedEpic.getDuration(),
                "Некорректная длительность эпика");
        assertEquals(newSubtask.getStartTime(), receivedEpic.getStartTime(),
                "Некорректное время начала эпика");
    }

    @Test // Проверка обновления эпика при обновлении подзадачи
    void testEpicUpdateWithSubtaskUpdate() throws IOException, InterruptedException {
        Epic epic = new Epic("Эпик 1", "Создание");
        long duration = 5;
        Subtask subtask = new Subtask("Тест 1", "Проверка создания",
                TaskStatus.NEW, duration, date);
        Subtask newSubtask = new Subtask(2,"Тест 2", "Проверка создания 2",
                TaskStatus.DONE, duration, date.plusMinutes(180));

        // Запрос на создание эпика
        responseManager.sendPostResponse("/epics", epic);

        // Запрос на создание подзадачи
        responseManager.sendPostResponse("/subtasks", subtask);

        // Запрос на обновление подзадачи
        responseManager.sendPostResponse("/subtasks/2", newSubtask);

        // Запрос на получение эпика
        HttpResponse<String> response = responseManager.sendGetResponse("/epics/1");

        assertEquals(200, response.statusCode(), "Некорректный код статуса");
        Epic receivedEpic = gson.fromJson(response.body(), Epic.class);

        assertEquals(1, receivedEpic.getTaskId(), "Некорректный ID эпика");
        assertEquals(epic.getTaskName(), receivedEpic.getTaskName(),
                "Некорректное имя эпика");
        assertEquals(epic.getTaskDescription(), receivedEpic.getTaskDescription(),
                "Некорректное описание эпика");
        assertEquals(newSubtask.getTaskStatus(), receivedEpic.getTaskStatus(),
                "Некорректный статус эпика");
        assertEquals(newSubtask.getDuration(), receivedEpic.getDuration(),
                "Некорректная длительность эпика");
        assertEquals(newSubtask.getStartTime(), receivedEpic.getStartTime(),
                "Некорректное время начала эпика");
    }
}
