package manager;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import tasks.Epic;
import tasks.Subtask;
import tasks.Task;
import tasks.TaskStatus;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
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
       boolean isDeleted = file.delete();
       if (isDeleted) {
           System.out.println("Файл " + file.getName() + " удален.");
       } else {
           System.out.println("Файл " + file.getName() + " не удален.");
       }
    }

    @TempDir
    Path tempDir;

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

    @Test // Проверка отсутствия задачи при загрузке
    void shouldNotLoadRemovedTask() {
        taskManager.createNewTask(task);
        taskManager.createNewEpic(epic);
        taskManager.createNewSubtask(epic, subtask);

        taskManager.deleteTaskById(task.getTaskId());

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

    @Test // Проверка отсутствия задач при загрузке
    void shouldNotLoadRemovedTasks() {
        taskManager.createNewTask(task);
        taskManager.createNewEpic(epic);
        taskManager.createNewSubtask(epic, subtask);

        taskManager.deleteAllTasks();

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

    @Test // Проверка отсутствия эпика при загрузке
    void shouldNotLoadRemovedEpic() {
        taskManager.createNewTask(task);
        taskManager.createNewEpic(epic);
        taskManager.createNewSubtask(epic, subtask);

        taskManager.deleteEpicById(epic.getTaskId());

        // Загружаем из файла
        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(file);

        // Проверяем задачи
        List<Task> tasks = loadedManager.getTasks();
        List<Epic> epics = loadedManager.getEpics();
        List<Subtask> subtasks = loadedManager.getSubtasks();

        assertAll(
                () -> assertEquals(1, tasks.size(), "Неверное количество задач"),
                () -> assertEquals(task, tasks.getFirst(), "Эпик не совпадает"),


                () -> assertEquals(0, epics.size(), "Неверное количество эпиков"),
                () -> assertTrue(epics.isEmpty(), "Эпик не удален"),

                () -> assertEquals(0, subtasks.size(), "Неверное количество подзадач"),
                () -> assertTrue(subtasks.isEmpty(), "Подзадача не удалена вместе с эпиком")
        );

    }

    @Test // Проверка отсутствия эпиков при загрузке
    void shouldNotLoadRemovedEpics() {
        taskManager.createNewTask(task);
        taskManager.createNewEpic(epic);
        taskManager.createNewSubtask(epic, subtask);

        taskManager.deleteAllEpics();

        // Загружаем из файла
        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(file);

        // Проверяем задачи
        List<Task> tasks = loadedManager.getTasks();
        List<Epic> epics = loadedManager.getEpics();
        List<Subtask> subtasks = loadedManager.getSubtasks();

        assertAll(
                () -> assertEquals(1, tasks.size(), "Неверное количество задач"),
                () -> assertEquals(task, tasks.getFirst(), "Эпик не совпадает"),


                () -> assertEquals(0, epics.size(), "Неверное количество эпиков"),
                () -> assertTrue(epics.isEmpty(), "Эпик не удален"),

                () -> assertEquals(0, subtasks.size(), "Неверное количество подзадач"),
                () -> assertTrue(subtasks.isEmpty(), "Подзадача не удалена вместе с эпиком")
        );

    }

    @Test // Проверка отсутствия подзадачи при загрузке
    void shouldNotLoadRemovedSubtask() {
        taskManager.createNewTask(task);
        taskManager.createNewEpic(epic);
        taskManager.createNewSubtask(epic, subtask);

        taskManager.deleteSubtaskById(subtask.getTaskId());

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

                () -> assertEquals(0, subtasks.size(), "Неверное количество подзадач"),
                () -> assertTrue(subtasks.isEmpty(), "Подзадача не удалена")
        );
    }

    @Test // Проверка отсутствия подзадач при загрузке
    void shouldNotLoadRemovedSubtasks() {
        taskManager.createNewTask(task);
        taskManager.createNewEpic(epic);
        taskManager.createNewSubtask(epic, subtask);

        taskManager.deleteAllSubtasks();

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

                () -> assertEquals(0, subtasks.size(), "Неверное количество подзадач"),
                () -> assertTrue(subtasks.isEmpty(), "Подзадача не удалена")
        );
    }

    @Test // Проверка загрузки обновленной задачи
    void shouldLoadUpdatedTask() {
        taskManager.createNewTask(task);
        Task task2 = new Task(task.getTaskId(), task.getTaskName(), task.getTaskDescription(), TaskStatus.DONE);
        taskManager.updateTask(task2);

        // Загружаем из файла
        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(file);

        // Проверяем задачи
        List<Task> tasks = loadedManager.getTasks();

        assertAll(
                () -> assertFalse(tasks.isEmpty(), "Задача не восстановилась"),
                () -> assertEquals(task2, tasks.getFirst(), "Задача не обновилась")
        );
    }

    @Test // Проверка загрузки обновленного эпика
    void shouldLoadUpdatedEpic() {
        taskManager.createNewEpic(epic);
        Epic epic2 = new Epic(epic.getTaskId(), epic.getTaskName() + "!", epic.getTaskDescription());
        taskManager.updateEpic(epic2);

        // Загружаем из файла
        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(file);

        // Проверяем эпики
        List<Epic> epics = loadedManager.getEpics();

        assertAll(
                () -> assertFalse(epics.isEmpty(), "Задача не восстановилась"),
                () -> assertEquals(epic2, epics.getFirst(), "Задача не обновилась")
        );
    }

    @Test // Проверка загрузки обновленной подзадачи
    void shouldLoadUpdatedSubtask() {
        taskManager.createNewEpic(epic);
        taskManager.createNewSubtask(epic, subtask);
        Subtask subtask2 = new Subtask(subtask.getTaskId(), subtask.getTaskName() + "!",
                                       subtask.getTaskDescription(), subtask.getTaskStatus());
        taskManager.updateSubtask(subtask2);

        // Загружаем из файла
        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(file);

        // Проверяем подзадачи
        List<Subtask> subtasks = loadedManager.getSubtasks();

        assertAll(
                () -> assertFalse(subtasks.isEmpty(), "Задача не восстановилась"),
                () -> assertEquals(subtask2, subtasks.getFirst(), "Задача не обновилась")
        );
    }

    @Test // Проверка обработки исключения в loadFromFile() при попытке прочитать нечитаемый файл
    void loadFromFile_shouldThrowUncheckedIOException() throws IOException {
        File unreadableFile = tempDir.resolve("file2.csv").toFile();

        assertFalse(unreadableFile.canRead(), "Файл не должен быть доступен для чтения");

        assertThrows(UncheckedIOException.class, () -> FileBackedTaskManager.loadFromFile(unreadableFile));
    }

    @Test // Проверка обработки исключения в save() при попытке чтения из несуществующей директории
    void save_shouldThrowManagerSaveException() {
        File nonExistentDir = tempDir.resolve("nonexistent").toFile();
        File newFile = new File(nonExistentDir, "file2.csv");

        FileBackedTaskManager manager = new FileBackedTaskManager(newFile);

        assertThrows(ManagerSaveException.class,
                () -> manager.createNewTask(task));
    }
}
