package manager;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tasks.Epic;
import tasks.Subtask;
import tasks.Task;
import tasks.TaskStatus;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class FileBackedTaskManagerTest {
    private FileBackedTaskManager taskManager;
    private Task task;
    private Epic epic;
    private Subtask subtask;

    private File file;

    @BeforeEach
    void beforeEach() {
        try {
            file = File.createTempFile("tasks", ".csv");
        } catch (IOException e) {
            throw new RuntimeException("Не удалось создать объекта менеджера",e);
        }
        taskManager = new FileBackedTaskManager(file);
        task = new Task( "Задача #1","Проверка", TaskStatus.NEW);
        epic = new Epic( "Эпик #1","Проверка");
        subtask = new Subtask("Подзадача #1", "Проверка", TaskStatus.IN_PROGRESS);
    }

    @AfterEach
    void afterEach() {
        file.delete();
    }

    @Test // Проверка сохранения и загрузки пустого файла
    void shouldSaveAndLoadEmptyManager() {
        // Сохраняем пустой менеджер
        FileBackedTaskManager savedManager = FileBackedTaskManager.loadFromFile(file);

        assertAll(
                () -> assertTrue(savedManager.getTasks().isEmpty(), "Список задач должен быть пустым"),
                () -> assertTrue(savedManager.getEpics().isEmpty(), "Список эпиков должен быть пустым"),
                () -> assertTrue(savedManager.getSubtasks().isEmpty(), "Список подзадач должен быть пустым")
        );
    }

    @Test // Проверка сохранения и загрузки нескольких разных задач
    void shouldSaveAndLoadTasks() {
        // Добавляем задачи в менеджер
        taskManager.createNewTask(task);
        taskManager.createNewEpic(epic);
        taskManager.createNewSubtask(epic, subtask);

        // Вывод содержимого файла
        try {
            System.out.println("Содержимое файла:");
            System.out.println(Files.readString(file.toPath()));
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Загружаем из файла
        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(file);

        // Проверяем задачи
        List<Task> tasks = loadedManager.getTasks();
        List<Epic> epics = loadedManager.getEpics();
        List<Subtask> subtasks = loadedManager.getSubtasks();

        assertAll(
                () -> assertEquals(1, tasks.size(), "Неверное количество задач"),
                () -> assertEquals(task, tasks.getFirst(), "Задача не совпадает"),

                () -> assertEquals(1, epics.size(), "Неверное количество эпиков"),
                () -> assertEquals(epic, epics.getFirst(), "Эпик не совпадает"),

                () -> assertEquals(1, subtasks.size(), "Неверное количество подзадач"),
                () -> assertEquals(subtask, subtasks.getFirst(), "Подзадача не совпадает"),
                () -> assertEquals(epic.getTaskId(), subtasks.getFirst().getEpicIdentifier(),
                        "ID эпика у подзадачи не совпадает")
        );
    }

    @Test
    void shouldNotLoadRemovedTask() {
        taskManager.createNewTask(task);
        taskManager.createNewEpic(epic);
        taskManager.createNewSubtask(epic, subtask);

        taskManager.deleteTaskById(task.getTaskId());

        // Загружаем из файла
        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(file);

        try {
            System.out.println("Содержимое файла:");
            System.out.println(Files.readString(file.toPath()));
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Проверяем задачи
        List<Task> tasks = loadedManager.getTasks();
        List<Epic> epics = loadedManager.getEpics();
        List<Subtask> subtasks = loadedManager.getSubtasks();

        assertAll(
                () -> assertEquals(0, tasks.size(), "Неверное количество задач"),
                () -> assertTrue(tasks.isEmpty(), "Задача не удалена"),

                () -> assertEquals(1, epics.size(), "Неверное количество эпиков"),
                () -> assertEquals(epic, epics.getFirst(), "Эпик не совпадает"),

                () -> assertEquals(1, subtasks.size(), "Неверное количество подзадач"),
                () -> assertEquals(subtask, subtasks.getFirst(), "Подзадача не совпадает"),
                () -> assertEquals(epic.getTaskId(), subtasks.getFirst().getEpicIdentifier(),
                        "ID эпика у подзадачи не совпадает")
        );
    }

    @Test
    void shouldNotLoadRemovedTasks() {
        taskManager.createNewTask(task);
        taskManager.createNewEpic(epic);
        taskManager.createNewSubtask(epic, subtask);

        try {
            System.out.println("Содержимое файла 1:");
            System.out.println(Files.readString(file.toPath()));
        } catch (IOException e) {
            e.printStackTrace();
        }

        taskManager.deleteAllTasks();

        try {
            System.out.println("Содержимое файла 2:");
            System.out.println(Files.readString(file.toPath()));
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Загружаем из файла
        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(file);

        // Проверяем задачи
        List<Task> tasks = loadedManager.getTasks();
        List<Epic> epics = loadedManager.getEpics();
        List<Subtask> subtasks = loadedManager.getSubtasks();

        assertAll(
                () -> assertEquals(0, tasks.size(), "Неверное количество задач"),
                () -> assertTrue(tasks.isEmpty(), "Задача не удалена"),

                () -> assertEquals(1, epics.size(), "Неверное количество эпиков"),
                () -> assertEquals(epic, epics.getFirst(), "Эпик не совпадает"),

                () -> assertEquals(1, subtasks.size(), "Неверное количество подзадач"),
                () -> assertEquals(subtask, subtasks.getFirst(), "Подзадача не совпадает"),
                () -> assertEquals(epic.getTaskId(), subtasks.getFirst().getEpicIdentifier(),
                        "ID эпика у подзадачи не совпадает")
        );
    }

}
